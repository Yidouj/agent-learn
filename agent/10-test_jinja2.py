from jinja2 import Template

# 能力1：变量注入
tpl = Template("你是一个{{ role }}，请{{ task }}")
print(tpl.render(role="天气助手", task="查询天气"))
# → 你是一个天气助手，请查询天气

# 能力2：条件逻辑
tpl2 = Template("""
{% if tools %}
你有以下工具可用：
{% for tool in tools %}
- {{ tool.name }}: {{ tool.description }}
{% endfor %}
{% else %}
你当前没有工具可用，请直接回答用户问题。
{% endif %}
""")

# 能力3：循环渲染
tools = [
    # {"name": "get_weather", "description": "查询天气"},
    # {"name": "get_time", "description": "获取当前时间"},
]
print(tpl2.render(tools=tools))




AGENT_PROMPT = """
你是一个 {{ agent_name }}。

## 角色
{{ role_description }}

## 可用工具
{% for tool in tools %}
### {{ tool.name }}
{{ tool.description }}
参数：
{% for param in tool.parameters %}
- {{ param.name }} ({{ param.type }}): {{ param.description }}
{% endfor %}
{% endfor %}

## 约束
{% for rule in constraints %}
- {{ rule }}
{% endfor %}

{% if examples %}
## 示例
{% for ex in examples %}
用户: {{ ex.user }}
助手: {{ ex.assistant }}
{% endfor %}
{% endif %}

## 输出格式
请用以下 JSON 格式回答：
{{ output_schema }}
"""

template = Template(AGENT_PROMPT)
prompt = template.render(
    agent_name="天气助手",
    role_description="专业的气象服务 Agent",
    tools=[
        {"name": "get_weather", "description": "查询指定城市天气",
         "parameters": [
            {"name": "city", "type": "string", "description": "城市名称"},
            {"name": "date", "type": "string", "description": "日期，默认今天"}
         ]}
    ],
    constraints=[
        "只能回答天气相关问题",
        "如果查询不到数据，如实告知用户",
        "输出必须是合法 JSON"
    ],
    examples=[
        {"user": "北京今天天气怎么样？",
         "assistant": '{"action":"call_tool","tool":"get_weather","args":{"city":"北京"}}'}
    ],
    output_schema='{"action": "call_tool|respond", "tool": "工具名", "args": {}, "answer": ""}'
)
print(prompt)
