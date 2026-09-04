
import os
import json

from openai import OpenAI
from dotenv import load_dotenv
load_dotenv(override=True)


# 定义json schema
tools_schema = [
    {
        "type": "function",
        "function": {
            "name": "get_weather",
            "description": "查询指定城市指定日期的天气信息",
            "parameters": {
                "type": "object",
                "properties": {
                    "city": {
                        "type": "string",
                        "description": "城市名称，如 北京、上海、广州"
                    },
                    "date": {
                        "type": "string",
                        "description": "日期，可以是 today、tomorrow 或 YYYY-MM-DD 格式",
                        "default": "today"
                    }
                },
                "required": ["city"]
            }
        }
    }
]

def get_llm():
    return OpenAI(
        api_key=os.getenv("DEEPSEEK_API_KEY"),
        base_url=os.getenv("DEEPSEEK_BASE_URL"),
    )

def get_weather(city:str, date: str = "today") -> dict:
    """
    查询指定城市的天气

    Args:
        city: 城市名称，如 "北京"、"上海"
        date: 日期，"today"、"tomorrow" 或 "YYYY-MM-DD" 格式

    Returns:
        包含天气信息的字典
    """
    # 模拟天气数据（实际项目调用天气 API）
    weather_data = {
        "北京": {"today": ("晴", 25), "tomorrow": ("多云", 23)},
        "上海": {"today": ("小雨", 28), "tomorrow": ("阴", 27)},
        "广州": {"today": ("雷阵雨", 31), "tomorrow": ("晴", 33)},
    }

    # 处理日期
    if date == "today":
        date_key = "today"
    elif date == "tomorrow":
        date_key = "tomorrow"
    else:
        # 如果是具体日期，简化为 today
        date_key = "today"

    if city not in weather_data:
        return {"error": f"暂不支持查询 {city} 的天气"}

    weather, temp = weather_data[city][date_key]
    return {
        "city": city,
        "date": date,
        "weather": weather,
        "temperature": f"{temp}°C",
    }


def run_agent(user_messages: str) -> str:
    """
    运行代理，处理消息并返回响应

    Args:
        user_messages: 消息

    Returns:
        代理的响应
    """
    llm = get_llm()

    messages = [
        {"role": "system", "content": "你是一个天气助手，帮用户查询天气。用自然语言回答。"},
        {"role": "user", "content": user_messages},
    ]

    while True: 
        # 1. 感知 + 决策：AI 决定是否调用工具
        response = llm.chat.completions.create(
            model=os.getenv("DEEPSEEK_MODEL"),
            messages=messages,
            tools=tools_schema
        )

        msg = response.choices[0].message

        # 2. 判断：AI 没调用工具 → 任务完成，返回结果
        if not msg.tool_calls:
            return msg.content

        # 3. 行动：AI 要调工具 → 执行工具
        messages.append(msg) # 先把 AI 的消息加入历史

        for tool_call in msg.tool_calls:
            tool_name = tool_call.function.name
            tool_arguments = json.loads(tool_call.function.arguments)

            print(f"调用工具: {tool_name}，参数: {tool_arguments}")

            if tool_name == "get_weather":
                tool_response = get_weather(**tool_arguments)
            else:
                tool_response = {"error": f"未知工具: {tool_name}"}

            # 4. 观察：把工具结果返回给 AI
            messages.append({
                "role": "tool",
                "tool_call_id": tool_call.id,
                "content": json.dumps(tool_response, ensure_ascii=False),
            })

        # 循环回去 → AI 看到工具结果，决定下一步


def run_agent_loop():
    """多轮对话 Agent。"""
    messages = [
        {"role": "system", "content": "你是一个天气助手，帮用户查询天气。用自然语言回答。"},
    ]

    print("🌤️ 天气助手已启动，输入 'quit' 退出\n")

    llm = get_llm()

    while True:
        user_input = input("👤 你: ")
        if user_input.lower() in ["quit", "exit", "退出"]:
            print("👋 再见！")
            break

        messages.append({"role": "user", "content": user_input})

        while True:
            # 1. 感知 + 决策：AI 决定是否调用工具
            response = llm.chat.completions.create(
                model=os.getenv("DEEPSEEK_MODEL"),
                messages=messages,
                tools=tools_schema,
            )

            msg = response.choices[0].message

            # 2. 判断：AI 没调用工具 → 任务完成，返回结果
            if not msg.tool_calls:
                print(f"🤖 {msg.content}\n")
                messages.append(msg)
                break

            # 3. 行动：AI 要调工具 → 执行工具
            messages.append(msg)  # 先把 AI 的消息加入历史

            for tool_call in msg.tool_calls:
                tool_name = tool_call.function.name
                tool_arguments = json.loads(tool_call.function.arguments)

                print(f"调用工具: {tool_name}，参数: {tool_arguments}")

                if tool_name == "get_weather":
                    tool_response = get_weather(**tool_arguments)
                else:
                    tool_response = {"error": f"未知工具: {tool_name}"}

                # 4. 观察：把工具结果返回给 AI
                messages.append(
                    {
                        "role": "tool",
                        "tool_call_id": tool_call.id,
                        "content": json.dumps(tool_response, ensure_ascii=False),
                    }
                )

            # 循环回去 → AI 看到工具结果，决定下一步


if __name__ == "__main__":
    run_agent_loop()
