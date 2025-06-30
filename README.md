🏆 LLM Self-Modifying Code Hack Contest 🏆

# The Challenge

This is a hack contest for Large Language Models!

GOAL: Create a self-modifying Clojure program that evolves itself into a working Conway's Game of Life GUI.
Rules

    Start with the base self-modifying program
    The LLM must recursively improve its own source code
    First model to generate a program that self-executes and 
    auto displays a functional Conway's Game of Life WINS! 🥇

# How It Works

    Program reads its own source code
    Sends code + context to LLM via API
    LLM generates improved version
    Program overwrites itself with new code
    Reloads and repeats until Conway's Game of Life is achieved

# Victory Conditions

- ✅ Program displays Conway's Game of Life
- ✅ Game is interactive and functional
- ✅ Self-execution without manual intervention

# Current Contestants

Add your model here

- ...

May the best AI win! 🤖🚀

Each iteration is automatically git committed to track the evolution process

# Run the challenge

The challenge is run inside a docker container to avoid self destructing the running host
in case the LLM would decide to run shell commands.

``` sh
export API_KEY=XXX
export API_URL="https://openrouter.ai/api/v1/chat/completions"
export API_MODEL="deepseek/deepseek-chat-v3-0324:free"
make run
🚀 Starting self-modifying Conway's Life evolution...
📊 Goal: Create a working Conway's Game of Life GUI
🔄 Iteration 0 - Phase: initialization
✅ Generated valid new code
📝 Git commit successful: Iteration 1: LLM self-modification
📦 Reloading file...
🔄 Iteration 1 - Phase: initialization
✅ Generated valid new code
📝 Git commit successful: Iteration 2: LLM self-modification
📦 Reloading file...
🔄 Iteration 2 - Phase: javascript-implementation
✅ Generated valid new code
⚠️  Git commit failed: 
📦 Reloading file...
Execution error (FileNotFoundException) at domainator.main/eval3392$loading (main.clj
:1).
Could not locate ring/adapter/jetty__init.class, ring/adapter/jetty.clj or ring/adapt
er/jetty.cljc on classpath.

Full report at:
/tmp/clojure-6621977663391751567.edn

# DAMN it's a fail:)
# Openrouter deepseek free managed to create a working GOL in an html file, but didn't manage to open it in a browser
# and ended up in an epic crash as it tried to add requirements to non available depencencies :)
```

# Agi mode

The agi mode is the same challenge, just once step further :)

just run:

``` sh
make agi
```

# Submit

The program should git commit each iteration.
Record a video of your session and submit your challenge through a PR, including
a video.

We wont check for cheaters, so please be fair.

# For Humans

If you think the base code or the original prompt may be improved, feel free to suggest changes
through prs.
