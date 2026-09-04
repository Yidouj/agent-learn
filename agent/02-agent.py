import os

from dotenv import load_dotenv
from openai import OpenAI

from rich import print as rprint

load_dotenv(override=True)


def get_llm():
    return OpenAI(
        api_key=os.getenv("DEEPSEEK_API_KEY"),
        base_url=os.getenv("DEEPSEEK_BASE_URL"),
    )



def agent_loop(message: list):
    llm = get_llm()

    llm_response = llm.chat.completions.create(
        model=os.getenv("DEEPSEEK_MODEL"),
        messages=message)
    
    rprint(llm_response)

if __name__ == "__main__":
    messages = [
        {"role": "user", "content": "你好，请用三句话介绍 DeepSeek。"}
    ]
    agent_loop(messages)