MVN := mvn
MVN_CLEAN := clean
MVN_PACKAGE := package

.DEFAULT_GOAL := all
.PHONY: all build clean

all: build

build:
	@echo "Build vite app..."
	@if [ ! -d "client/node_modules" ]; then \
		echo "Install dependencies first!"; \
		exit 1; \
	fi
	@cd client && npm run build
	@echo "Building Maven project..."
	@cd app && $(MVN) $(MVN_PACKAGE)
	@cp app/target/*.jar release

clean:
	@echo "Cleaning Maven project..."
	@cd app && $(MVN) $(MVN_CLEAN)
	@echo "Cleaning vite app..."
	@cd client && rm -rf dist
	@rm -rf release/*.jar

