# Check for required environment variables
default: help

check-env:
	@echo "Checking required environment variables..."
	@test -n "$(API_KEY)" || (echo "ERROR: API_KEY environment variable is required" && exit 1)
	@test -n "$(API_URL)" || (echo "ERROR: API_URL environment variable is required" && exit 1)
	@test -n "$(API_MODEL)" || (echo "ERROR: API_MODEL environment variable is required" && exit 1)
	@echo "✅ All required environment variables are set"

docker-run: check-env
	@docker run --rm -it \
		-e API_KEY \
		-e API_URL \
		-e API_MODEL \
		-v $$PWD:/src \
		-v gol-m2-cache:/root/.m2 \
		-w /src \
		nixery.dev/bash/clojure/git/coreutils \
		bash -c "git config --global user.email 'llm@challenge.com' && \
		         git config --global user.nam 'LLM Challenge' && \
				 cp -Rf /src /tmp/ && cd /tmp/src/ && \
		         clj -M:run"

# Shorter alias
run: docker-run

# Help target
help:
	@echo "Usage:"
	@echo "  make help         - This help"
	@echo "  make run          - Run the LLM challenge (checks env vars first)"
	@echo "  make docker-run   - Same as 'run'"
	@echo "  make check-env    - Check if required environment variables are set"
	@echo ""
	@echo "Required environment variables:"
	@echo "  API_KEY    - Your LLM API key"
	@echo "  API_URL    - LLM API endpoint URL"
	@echo "  API_MODEL  - LLM model to use"
	@echo ""
	@echo "Example:"
	@echo "  export API_KEY=your-key-here"
	@echo "  export API_URL=https://openrouter.ai/api/v1/chat/completions"
	@echo "  export API_MODEL=deepseek/deepseek-chat-v3-0324:free"
	@echo "  make run"

.PHONY: check-env docker-run run help
