import os
from dotenv import load_dotenv
from langchain.messages import SystemMessage,HumanMessage,AIMessage
from langchain.chat_models import init_chat_model

from rich import print as rprint

load_dotenv(override=True)

llm = init_chat_model(
    model="deepseek-v4-flash",
    model_provider="deepseek",
    model_kwargs={"tools": [
        {
  "type": "function",
  "function": {
    "name": "get_weather",
    "description": "获取指定城市的实时天气信息",
    "parameters": {
      "type": "object",
      "properties": {
        "city": {
          "type": "string",
          "description": "城市名称，例如：北京、上海、广州"
        },
        "unit": {
          "type": "string",
          "enum": ["celsius", "fahrenheit"],
          "description": "温度单位，celsius 表示摄氏度，fahrenheit 表示华氏度"
        }
      },
      "required": ["city"]
    }
  }
}

    ]},
    )

messages = [
    {"role": "system", "content": "你是一个有帮助的 AI 助手。"},
    {"role": "user", "content": "明天北京天气怎么样"},
]

response = llm.invoke(messages)

rprint(response)
