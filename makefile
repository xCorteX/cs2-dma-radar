MVN := mvn
MVN_CLEAN := clean
MVN_PACKAGE := package

.DEFAULT_GOAL := all

all: build

build:
	@echo "Build vite app..."
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

# Define phony targets
.PHONY: all build clean
