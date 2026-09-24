.PHONY: help run-web build-web run-web-prod test build-android install-android clean compile

# Default target when just running 'make'
help:
	@echo "=========================================================="
	@echo "           TUMBAS POS - BUILD & RUN COMMANDS              "
	@echo "=========================================================="
	@echo " Web (WasmJs):"
	@echo "   make run-web         - Jalankan Web Wasm Dev Server (http://localhost:8080)"
	@echo "   make build-web       - Build bundle produksi Web Wasm"
	@echo "   make run-web-prod    - Jalankan Web Wasm Production Server"
	@echo ""
	@echo " Android:"
	@echo "   make build-android   - Build Debug APK Android"
	@echo "   make install-android - Install Debug APK ke device/emulator"
	@echo ""
	@echo " Quality & Maintenance:"
	@echo "   make compile         - Kompilasi semua target (Android & Wasm)"
	@echo "   make test            - Jalankan Unit Tests"
	@echo "   make clean           - Bersihkan cache build Gradle"
	@echo "=========================================================="

# Jalankan Web Wasm Development Server (Live Reload)
run-web:
	./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Build bundle produksi Web Wasm
build-web:
	./gradlew :composeApp:wasmJsBrowserDistribution

# Jalankan Web Wasm Production Server
run-web-prod:
	./gradlew :composeApp:wasmJsBrowserProductionRun

# Build APK Android Debug
build-android:
	./gradlew :composeApp:assembleDebug

# Install APK Android ke Device / Emulator yang terhubung
install-android:
	./gradlew :composeApp:installDebug

# Kompilasi kode multiplatform Android & Wasm
compile:
	./gradlew :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinWasmJs

# Jalankan seluruh Unit Test
test:
	./gradlew :composeApp:testDebugUnitTest

# Bersihkan build cache
clean:
	./gradlew clean
