import httpx

def web_search(query: str) -> str:
    """
    搜索互联网内容

    Args:
        query (str): 搜索查询字符串

    Returns:
        str: 搜索结果的总结
    """
    return "这是测试"

if __name__ == "__main__":
    res = web_search("明天天气")
    result = res.send(None)  # 运行协程并获取结果
    print(result)
