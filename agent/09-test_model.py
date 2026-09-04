from pydantic import BaseModel, Field
from typing import List

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

class WeatherResponse(BaseModel):
    city: str = Field(description="城市名称")
    weather: str = Field(description="天气概况")
    temperature: float = Field(description="温度（摄氏度）")
    advice: str = Field(description="出行建议")
    clothing: List[str] = Field(description="穿衣推荐列表")


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
        "temperature": temp,
    }


if __name__ == "__main__":
    llm = get_llm()
    messages = [
        {
            "role": "system",
            "content": "你是一个天气助手，帮用户查询天气。用自然语言回答。\n"
            "请严格返回 JSON，不要返回 Markdown 或其他说明文字。\n"
            "JSON 必须包含 city(城市名称)、weather(天气概况)、temperature(温度（摄氏度)，是数值类型)、advice(出行建议)、clothing(穿衣推荐列表) 字段。",
        },
        {"role": "user", "content": "北京今天天气怎么样？"},
    ]

    while True:

        response = llm.chat.completions.create(
            model=os.getenv("DEEPSEEK_MODEL"),
            messages=messages,
            tools=tools_schema,
            response_format={
                'type': 'json_object'
            }
        )

        msg = response.choices[0].message

        if not msg.tool_calls:
            print(f"🤖 结果：{msg.content}\n")
            result = WeatherResponse.model_validate_json(msg.content)
            print(f"{result.city}: {result.weather}, {result.temperature}°C")
            print(f"建议: {result.advice}")
            print(f"穿衣: {', '.join(result.clothing)}")
            break

        messages.append(msg) 

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