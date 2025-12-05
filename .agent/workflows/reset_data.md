---
description: Clear App Data (Reset Database)
---
This workflow clears the application data for TumbasPOS, effectively resetting the database and all local preferences. Use this if you suspect corrupted data or want to trigger a fresh seeding of categories/products.

1. Ensure your device/emulator is connected.
2. Run the following command:

// turbo
adb shell pm clear com.tumbaspos.app

3. Restart the application manually.
