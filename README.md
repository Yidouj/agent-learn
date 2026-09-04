# agent-learn

学习 Agent 开发的 Python 示例项目，同时包含一个独立的 Java / Spring Boot Seata 商城示例。

## 项目结构

```text
.
├── pyproject.toml              # Python 项目元数据和直接依赖
├── uv.lock                     # uv 解析后的锁定依赖，建议提交到 Git
├── main.py                     # 最小 Python 入口示例
├── agent/
│   ├── 01-test.py              # LangChain + DeepSeek 示例
│   ├── 02-agent.py             # OpenAI 兼容接口示例
│   ├── 03-agent.py             # LangChain 工具调用示例
│   └── 04-base.py              # Python 基础练习
├── visio/                      # Visio / SVG 图形生成脚本
└── mall-seata-demo/            # 独立的 Maven 多模块 Java 示例
```

Python 项目要求 **Python 3.11 或更高版本**。当前 `pyproject.toml` 中声明的直接依赖包括：

- `langchain`
- `langchain-deepseek`
- `python-dotenv`

项目当前还包含使用 `openai`、`rich` 和 `vsdx` 的脚本。它们可能由现有环境中的其他依赖间接安装，但如果要稳定运行对应脚本，应按“引入新包”的流程将它们声明为直接依赖，而不要依赖偶然存在的传递依赖。

> `mall-seata-demo` 是 Java 项目，使用 Maven 管理依赖和测试；请参考 [mall-seata-demo/README.md](mall-seata-demo/README.md)，不要使用 uv 管理它的依赖。

## 安装 uv

Windows PowerShell 可使用官方安装脚本：

```powershell
powershell -ExecutionPolicy ByPass -c "irm https://astral.sh/uv/install.ps1 | iex"
```

安装后重新打开终端，并确认版本：

```powershell
uv --version
```

如已安装 uv，可升级到最新版：

```powershell
uv self update
```

Linux / macOS 可使用：

```bash
curl -LsSf https://astral.sh/uv/install.sh | sh
uv --version
```

## 首次初始化开发环境

在项目根目录（包含 `pyproject.toml` 的目录）执行：

```powershell
uv sync
```

`uv sync` 会自动完成以下工作：

1. 检查 Python 版本是否满足 `requires-python = ">=3.11"`；
2. 创建或更新项目虚拟环境 `.venv`；
3. 根据 `pyproject.toml` 和 `uv.lock` 安装依赖；
4. 在依赖发生变化时更新 `uv.lock`。

不需要手动执行 `python -m venv .venv`、`pip install -r requirements.txt`，本项目也没有 `requirements.txt`。

如果系统中还没有合适的 Python，可以让 uv 安装并使用 Python 3.11：

```powershell
uv python install 3.11
uv sync --python 3.11
```

检查当前项目解释器和依赖树：

```powershell
uv run python --version
uv tree
```

### 是否需要激活虚拟环境

不必激活，推荐直接使用 `uv run`，这样可以确保命令使用项目自己的 `.venv`：

```powershell
uv run python main.py
```

如果希望在当前终端中手动激活：

```powershell
# PowerShell
.\.venv\Scripts\Activate.ps1

# cmd.exe
.venv\Scripts\activate.bat
```

激活后可使用 `python`，但团队脚本和文档仍建议使用 `uv run python ...`，避免误用系统 Python。退出环境：

```powershell
deactivate
```

## 运行项目中的 Python 示例

先准备环境变量文件。复制 `.env.example`（如果项目提供）或在项目根目录创建 `.env`：

```dotenv
DEEPSEEK_MODEL=deepseek-v4-flash
DEEPSEEK_API_KEY=替换为自己的密钥
DEEPSEEK_BASE_URL=https://api.deepseek.com
```

`.env` 已被 `.gitignore` 忽略，**不要把真实 API Key 提交到 Git，也不要在聊天记录、截图或公开日志中暴露密钥**。如果密钥曾经泄露，应立即在服务商后台撤销并重新生成。

运行最小示例：

```powershell
uv run python main.py
```

运行 Agent 示例：

```powershell
uv run python agent/01-test.py
uv run python agent/02-agent.py
uv run python agent/03-agent.py
uv run python agent/04-base.py
```

运行 Visio / SVG 生成脚本：

```powershell
uv run python visio/build_ptga_vsdx.py
uv run python visio/build_ptga_from_svg.py
```

其中 Visio 脚本依赖 `vsdx`，Agent 示例还使用 `openai` 和 `rich`。若出现 `ModuleNotFoundError`，请按下面的依赖引入流程补充直接依赖，而不要直接在 `.venv` 中手工安装。

## 引入新包的标准流程

### 1. 添加运行时依赖

例如项目代码新增了 `httpx`：

```powershell
uv add httpx
```

指定版本约束：

```powershell
uv add "httpx>=0.28,<1"
```

指定精确版本（只有确实需要固定版本时使用）：

```powershell
uv add "httpx==0.28.1"
```

`uv add` 会同时完成以下操作：

- 修改 `pyproject.toml` 的 `[project].dependencies`；
- 重新解析依赖；
- 更新 `uv.lock`；
- 将包安装到项目 `.venv`。

### 2. 为当前项目补齐已有脚本的直接依赖

按照当前代码的实际导入情况，可以使用：

```powershell
uv add openai rich vsdx
```

说明：包的安装名和 Python 的导入名不一定相同，例如安装包 `python-dotenv` 的导入名是 `dotenv`。添加前应以包的官方文档和实际 import 为准。

### 3. 添加开发依赖

测试、格式化、类型检查等只服务于开发流程的包，不应放入运行时依赖：

```powershell
uv add --dev pytest ruff
```

也可以指定开发依赖版本：

```powershell
uv add --dev "pytest>=8,<9"
```

开发依赖会写入 `dependency-groups.dev`，安装默认开发环境时会一并安装。

### 4. 验证并提交变更

引入依赖后至少执行：

```powershell
uv sync
uv lock --check
uv run python main.py
```

如果新增包对应某个具体脚本，还要运行该脚本或其测试。例如：

```powershell
uv run python agent/02-agent.py
uv run python visio/build_ptga_vsdx.py
```

确认通过后，将 **`pyproject.toml` 和 `uv.lock` 一起提交**。不要只提交其中一个，否则其他开发者或 CI 可能得到不同的依赖解析结果。

## 依赖维护命令速查

| 目的 | 命令 |
| --- | --- |
| 安装 / 同步项目依赖 | `uv sync` |
| 严格按锁文件同步，不重新解析 | `uv sync --locked` |
| 运行项目命令 | `uv run <command>` |
| 添加运行时依赖 | `uv add <package>` |
| 添加开发依赖 | `uv add --dev <package>` |
| 删除依赖 | `uv remove <package>` |
| 查看依赖树 | `uv tree` |
| 查看已安装包 | `uv pip list` |
| 检查锁文件是否最新 | `uv lock --check` |
| 重新生成锁文件 | `uv lock` |
| 升级全部可升级依赖 | `uv lock --upgrade` |
| 升级指定依赖 | `uv lock --upgrade-package <package>` |
| 查看 Python 版本 | `uv python list` |
| 安装 Python 版本 | `uv python install 3.11` |

### 更新依赖的建议流程

日常开发优先使用：

```powershell
uv add "package>=版本范围"
uv sync
uv lock --check
```

需要主动升级已有依赖时，先查看变更影响，再执行：

```powershell
uv lock --upgrade-package langchain
uv sync
uv run python main.py
```

升级后应重点检查 API 兼容性，并将锁文件变更与代码变更放在同一个提交中。不要为了“让安装通过”随意删除版本约束、降低版本或删除 `uv.lock`。

## 镜像源说明

本项目在 `pyproject.toml` 中配置了清华 PyPI 镜像作为默认源：

```toml
[[tool.uv.index]]
url = "https://pypi.tuna.tsinghua.edu.cn/simple"
default = true
```

因此生成的 `uv.lock` 中会记录该源。若本地或 CI 无法访问该镜像，可临时指定其他源进行排查，例如：

```powershell
uv sync --index https://pypi.org/simple
```

长期更换源时，应同步评估并提交配置和锁文件变化；不要在不同机器上随意混用源后提交未经验证的锁文件。

## 团队协作约定

- Python 依赖的唯一事实来源是 `pyproject.toml`；可复现安装依赖 `uv.lock`。
- 新包必须通过 `uv add` 引入，不要直接运行裸 `pip install`，也不要手改 `uv.lock`。
- 运行 Python 命令统一优先使用 `uv run`。
- 运行时依赖和开发依赖分开声明；没有运行时用途的工具使用 `uv add --dev`。
- 提交依赖变更时同时提交 `pyproject.toml` 与 `uv.lock`，并附上验证命令和结果。
- 不提交 `.env`、`.venv`、缓存、构建产物或包含凭据的文件。
- `mall-seata-demo` 的 Java 依赖继续使用 Maven：在该目录执行 `mvn test` 或参考其 README，不要把 Java 依赖添加到 uv 配置中。

## 常见问题

### `uv` 命令找不到

重新打开终端，使 uv 的安装目录加入 `PATH`；仍无法解决时，检查安装目录是否已加入用户环境变量。

### `No solution found` 或版本冲突

先查看完整依赖树和当前约束：

```powershell
uv tree
uv lock
```

然后检查新包要求的 Python 版本及其与现有 `langchain` 相关包的兼容范围。必要时收窄或调整 `uv add` 时的版本约束，并重新运行测试。

### 只想安装，不想修改项目声明

临时工具可以使用：

```powershell
uvx <tool> ...
```

但项目运行时依赖不要用 `uvx` 或手工 `uv pip install` 代替 `uv add`；否则依赖不会记录在 `pyproject.toml`，其他环境无法复现。

### CI / 新机器安装

使用锁文件进行严格同步：

```powershell
uv sync --locked
uv run python main.py
```

`--locked` 会在 `pyproject.toml` 与 `uv.lock` 不一致时直接失败，帮助尽早发现忘记更新或提交锁文件的问题。
