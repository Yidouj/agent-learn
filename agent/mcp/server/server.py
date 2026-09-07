from mcp.server.mcpserver import MCPServer

server = MCPServer(
    name = "server",
    instructions="提供基础计算"
)

@server.tool()
def add(a: int, b: int) -> int:
    """
    计算两个整数的和

    Args:
        a (int): 第一个整数
        b (int): 第二个整数

    Returns:
        int: 两个整数的和
    """
    return a + b

@server.resource("greeting://{name}")
def greeting(name: str) -> str:
    """
    返回一个问候消息

    Args:
        name (str): 用户的名字

    Returns:
        str: 问候消息
    """
    return f"Hello, {name}!"

@server.prompt()
def calculate_prompt(question: str) -> str:
    """生成计算任务提示词。"""
    return f"请使用可用的计算工具解决这个问题：{question}"


if __name__ == "__main__":
    server.run(transport="stdio")