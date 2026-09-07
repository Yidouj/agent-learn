import httpx
from mcp.server.mcpserver import MCPServer

app = MCPServer('web_search')

# @app.tool()
# async def web_search(query: str) -> str:
#     """
#     搜索互联网内容

#     Args:
#         query (str): 搜索查询字符串

#     Returns:
#         str: 搜索结果的总结
#     """
#     # 使用 httpx 进行网络请求
#     async with httpx.AsyncClient() as client:
#         response = await client.get(f"https://www.google.com/search?q={query}")
#         # 这里可以根据需要解析 response.text 来提取搜索结果
#         # 例如，可以使用 BeautifulSoup 或正则表达式来提取相关信息
#         # 这里只是一个简单的示例，返回原始的 HTML 响应
#         return response.text

@app.tool()
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
    app.run(transport='stdio')
