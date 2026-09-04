import os
from dotenv import load_dotenv
from langchain.messages import SystemMessage,HumanMessage,AIMessage
from langchain_deepseek import ChatDeepSeek

from rich import print as rprint

load_dotenv(override=True)

llm = ChatDeepSeek(
    model="deepseek-v4-flash",
    api_key=os.getenv("DEEPSEEK_API_KEY"),
)

messages = [
    {"role": "system", "content": "你是一个有帮助的 AI 助手。"},
    {"role": "user", "content": "你好，请用三句话介绍 DeepSeek。"},
]

response = llm.invoke(messages)
# for chunk in response:
#     print(chunk.content, end="", flush=True)
print(type(response))
rprint(response)
