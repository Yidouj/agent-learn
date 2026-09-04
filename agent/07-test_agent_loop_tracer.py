
import os
import json

import time
import logging

from openai import OpenAI
from dotenv import load_dotenv
load_dotenv(override=True)

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("weather_agent")


class AgentTracer:
    """Agent 调用链追踪器，用于记录每次工具调用的上下文信息"""

    def __init__(self):
        self.loops = []
        self.total_tokens = 0
        self.total_time = 0.0
        self.tool_calls_count = 0

    def record_loop(self, loop_num: int, response, loop_time: float, tool_names: list = None):
        usage = response.usage
        loop_info = {
            "loop": loop_num,
            "prompt_tokens": usage.prompt_tokens if usage else 0,
            "completion_tokens": usage.completion_tokens if usage else 0,
            "total_tokens": usage.total_tokens if usage else 0,
            "time_seconds": round(loop_time, 2),
            "tool_calls": tool_names or [],
            "has_tool_call": len(tool_names or []) > 0,
         }

        self.loops.append(loop_info)
        self.total_tokens += loop_info["total_tokens"]
        self.total_time += loop_time 
        if tool_names:
            self.tool_calls_count += len(tool_names)       

    def print_report(self):
        """打印分析报告"""
        print("\n" + "="*50)
        print("📊 Agent 调用链分析报告")
        print("="*50)
        print(f"总循环次数: {len(self.loops)}")
        print(f"总工具调用: {self.tool_calls_count} 次")
        print(f"总 token 消耗: {self.total_tokens}")
        print(f"总耗时: {round(self.total_time, 2)}s")
        print(f"\n--- 每次循环详情 ---")
        for loop in self.loops:
            role = "🔧 工具调用" if loop["has_tool_call"] else "💬 最终回答"
            print(f"  循环 {loop['loop']}: {role}")
            print(f"    tokens: {loop['total_tokens']} (prompt: {loop['prompt_tokens']}, completion: {loop['completion_tokens']})")
            print(f"    延迟: {loop['time_seconds']}s")
            if loop['tool_calls']:
                print(f"    工具: {', '.join(loop['tool_calls'])}")
        print("="*50)

        # 成本估算（以 GPT-4o-mini 为例）
        cost_per_1k_input = 0.15 / 1000   # $0.15/1M tokens → $0.00015/1K
        cost_per_1k_output = 0.60 / 1000  # $0.60/1M tokens → $0.0006/1K

        input_tokens = sum(l["prompt_tokens"] for l in self.loops)
        output_tokens = sum(l["completion_tokens"] for l in self.loops)
        estimated_cost = input_tokens * cost_per_1k_input + output_tokens * cost_per_1k_output
        print(f"💰 预估成本: ${estimated_cost:.6f}")




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
    },
    {
        "type": "function",
        "function": {
            "name": "plan_trip",
            "description": "根据城市和天气情况规划出行建议，需要天气信息作为输入",
            "parameters": {
                "type": "object",
                "properties": {
                    "city": {
                        "type": "string",
                        "description": "目的地城市，如 北京、上海、广州"
                    },
                    "weather_condition": {
                        "type": "string",
                        "description": "天气状况，如 晴、多云、小雨、雷阵雨"
                    },
                    "days": {
                        "type": "integer",
                        "description": "出行天数，默认1天",
                        "default": 1
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

    if city not in weather_data:
        return {
            "error": f"不支持查询 {city} 的天气，目前支持：北京、上海、广州",
            "supported_cities": list(weather_data.keys()),
        }

    # 模拟：随机超时（20%概率）
    import random
    if random.random() < 0.2:
        raise TimeoutError("天气服务响应超时，请稍后重试")

    weather, temp = weather_data[city].get(date, weather_data[city]["today"])  # 默认返回今天的天气
    return {
        "city": city,
        "date": date,
        "weather": weather,
        "temperature": f"{temp}°C",
    }

def plan_trip(city: str, weather_condition: str = "", days: int = 1) -> dict:
    """
    根据城市和天气情况规划出行建议

    Args:
        city: 目的地城市
        weather_condition: 天气情况（晴/多云/小雨/雷阵雨等）
        days: 出行天数

    Returns:
        行程建议字典
    """
    trip_data = {
        "北京": {
            "晴": "适合去故宫、颐和园，推荐户外游览",
            "多云": "适合逛博物馆、胡同，室内外均可",
            "小雨": "建议参观国家博物馆、798艺术区，以室内为主",
            "雷阵雨": "强烈建议室内活动：故宫室内展区、国家大剧院",
        },
        "上海": {
            "晴": "外滩散步、豫园游览、迪士尼乐园",
            "多云": "南京路逛街、田子坊艺术区",
            "小雨": "上海博物馆、环球金融中心观光厅",
            "雷阵雨": "室内商场、上海大剧院",
        },
        "广州": {
            "晴": "白云山登山、珠江夜游",
            "多云": "陈家祠、沙面岛散步",
            "小雨": "广州图书馆、广东省博物馆",
            "雷阵雨": "天河城购物中心、室内美食探店",
        },
    }

    if city not in trip_data:
        return {
            "error": f"暂不支持 {city} 的行程规划",
            "supported_cities": list(trip_data.keys()),
        }

    if not weather_condition:
        return {
            "city": city,
            "note": "请先查询天气，我再给出更精准的行程建议",
            "general_tip": trip_data[city].get("晴", "建议户外游览"),
        }

    recommendation = trip_data[city].get(
        weather_condition,
        "建议根据天气灵活安排室内外活动"
    )

    return {
        "city": city,
        "weather_condition": weather_condition,
        "recommendation": recommendation,
        "days": days,
    }

def execute_tool_with_retry(tool_name: str, tool_arguments: dict, max_retries: int = 3) -> dict:
    """
    执行工具，带指数退避重试

    Args:
        tool_name: 工具名
        tool_arguments: 工具参数
        max_retries: 最大重试次数

    Returns:
        工具结果字典（成功或错误信息）
    """
    for attempt in range(max_retries):
        try:
            if tool_name == "get_weather":
                result =  get_weather(**tool_arguments)
            elif tool_name == "plan_trip":
                result = plan_trip(**tool_arguments)
            else:
                result = {"error": f"未知工具: {tool_name}"}
            return result
        except TimeoutError as e:
            if attempt < max_retries:
                wait_time = 2 ** attempt  # 指数退避
                logger.warning(f"⚠️ 第 {attempt+1} 次重试，等待 {wait_time}s: {e}")
                time.sleep(wait_time)
            else:
                logger.error(f"工具 {tool_name} 调用失败，达到最大重试次数: {e}")
                return {
                    "error": f"{tool_name}服务暂时不可用，已重试 {max_retries} 次。建议稍后再试。",
                    "retry_attempts": max_retries,
                }

        except json.JSONDecodeError as e:
            # 不可重试的错误 → 直接降级
            logger.error(f"❌ 参数解析失败（不可重试）: {e}")
            return {
                "error": f"参数格式错误，无法解析: {str(e)}",
                "raw_args": str(tool_arguments),
            }

        except Exception as e:
            # 未知异常 → 降级，不暴露内部细节
            logger.error(f"❌ 未知异常: {type(e).__name__}: {e}")
            return {
                "error": "工具执行遇到未知错误，请稍后再试或换个方式提问。",
            }
        
def run_agent_with_tracer(user_message: str) -> str:
    """
    带调用链追踪的 Agent

    Args:
        user_message: 消息

    Returns:
        代理的响应
    """
    tracer = AgentTracer()

    messages = [
        {
            "role": "system",
            "content": "你是一个出行助手，可以查询天气和规划行程。",
        },
        {"role": "user", "content": user_message},
    ]

    tool_map = {
        "get_weather": get_weather,
        "plan_trip": plan_trip,
    }

    loop_num = 0
    while True:
        loop_num += 1
        start_time = time.time()

        # 1. 感知 + 决策：AI 决定是否调用工具
        response = get_llm().chat.completions.create(
            model=os.getenv("DEEPSEEK_MODEL"),
            messages=messages,
            tools=tools_schema
        )
        loop_time = time.time() - start_time
        msg = response.choices[0].message

        tool_names = [tc.function.name for tc in (msg.tool_calls or [])]
        tracer.record_loop(loop_num, response, loop_time, tool_names)

        # 2. 判断：AI 没调用工具 → 任务完成，返回结果
        if not msg.tool_calls:
            tracer.print_report()
            return msg.content

        messages.append(msg)  # 先把 AI 的消息加入历史

        for tool_call in msg.tool_calls:
            tool_name = tool_call.function.name
            try:
                tool_arguments = json.loads(tool_call.function.arguments)
            except json.JSONDecodeError:
                # AI 返回了无效 JSON → 直接告诉 AI
                tool_arguments = {}
                logger.warning(f"⚠️ AI 返回无效参数")

            print(f"调用工具: {tool_name}，参数: {tool_arguments}")

            # 3. 行动：执行工具，带重试机制
            tool_response = execute_tool_with_retry(tool_name, tool_arguments)

            print(f"📦 结果: {tool_response}")

            # 4. 观察：把工具结果返回给 AI
            messages.append({
                "role": "tool",
                "tool_call_id": tool_call.id,
                "content": json.dumps(tool_response, ensure_ascii=False),
            })


if __name__ == "__main__":
    print(run_agent_with_tracer("北京明天适不适合出游？"))
    
