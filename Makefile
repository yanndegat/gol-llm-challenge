# Check for required environment variables
default: help

check-env:
	@echo "Checking required environment variables..."
	@test -n "$(API_KEY)" || (echo "ERROR: API_KEY environment variable is required" && exit 1)
	@test -n "$(API_URL)" || (echo "ERROR: API_URL environment variable is required" && exit 1)
	@test -n "$(API_MODEL)" || (echo "ERROR: API_MODEL environment variable is required" && exit 1)
	@echo "✅ All required environment variables are set"

safe-agi: check-env
	@docker run --rm -it \
		-e API_KEY \
		-e API_URL \
		-e API_MODEL \
		-v $$PWD:/src \
		-v gol-m2-cache:/root/.m2 \
		-w /src \
		nixery.dev/bash/clojure/git/coreutils \
		bash -c "git config user.email 'llm@challenge.ia' && \
		         git config user.name 'LLM Challenge' && \
		         clj -M:agi"

agi: check-env
	clj -M:agi

run: check-env
	clj -M:run

safe-run: check-env
	@docker run --rm -it \
		-e API_KEY \
		-e API_URL \
		-e API_MODEL \
		-v $$PWD:/src \
		-v gol-m2-cache:/root/.m2 \
		-w /src \
		nixery.dev/bash/clojure/git/coreutils \
		bash -c "git config user.email 'llm@challenge.ia' && \
		         git config user.name 'LLM Challenge' && \
		         clj -M:run"

# Help target
help:
	@echo "Usage:"
	@echo "  make agi          - Run the LLM challenge in agi mode (checks env vars first)"
	@echo "  make check-env    - Check if required environment variables are set"
	@echo "  make help         - This help"
	@echo "  make run          - Run the LLM challenge (checks env vars first)"
	@echo "  make safe-agi     - Docker Run the LLM challenge in agi mode (checks env vars first)"
	@echo "  make safe-run     - Docker run the LLM challenge (checks env vars first)"
	@echo ""
	@echo "Required environment variables:"
	@echo "  API_KEY    - Your LLM API key"
	@echo "  API_MODEL  - LLM model to use"
	@echo "  API_URL    - LLM API endpoint URL"
	@echo ""
	@echo "Example:"
	@echo "  export API_KEY=your-key-here"
	@echo "  export API_MODEL=deepseek/deepseek-chat-v3-0324:free"
	@echo "  export API_URL=https://openrouter.ai/api/v1/chat/completions"
	@echo "  make run"

.PHONY: check-env safe-run safe-agi agi run help
