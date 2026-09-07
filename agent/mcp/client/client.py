import asyncio
import sys
from pathlib import Path

from mcp import ClientSession, StdioServerParameters
from mcp.client.stdio import stdio_client


async def main():
    server_file = Path(__file__).resolve().parent.parent / "server" / "server.py"
    print(f"启动服务器：{server_file}")
    server_params = StdioServerParameters(
        command=sys.executable,
        args=[str(server_file)],
    )

    # 创建 stdio客户端
    async with stdio_client(server_params) as (read_stream, write_stream):
        # 创建ClientSession对象
        async with ClientSession(read_stream, write_stream) as session:
            initialization = await session.initialize()
            print(f"已连接：{initialization.server_info.name}")

            tools = await session.list_tools()
            for tool in tools.tools:
                print(f"发现工具：{tool.name} - {tool.description}")

            result = await session.call_tool(
                "add",
                arguments={"a": 20, "b": 22},
            )

            if result.is_error:
                raise RuntimeError(f"工具调用失败：{result.content}")

            for content in result.content:
                if content.type == "text":
                    print(f"调用结果：{content.text}")


if __name__ == "__main__":
    asyncio.run(main())