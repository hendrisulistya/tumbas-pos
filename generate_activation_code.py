
import hmac
import hashlib
import sys
import os

def get_secret():
    try:
        with open('local.properties', 'r') as f:
            for line in f:
                if 'APP_SECRET' in line:
                    # Extract value between quotes
                    parts = line.split('=')
                    if len(parts) > 1:
                        return parts[1].strip().strip('"')
    except FileNotFoundError:
        print("Error: local.properties not found.")
        sys.exit(1)
    return None

def generate_code(app_id, secret):
    if not secret:
        print("Error: APP_SECRET not found in local.properties")
        sys.exit(1)
    
    # Calculate HMAC-SHA256
    h = hmac.new(secret.encode('utf-8'), app_id.encode('utf-8'), hashlib.sha256)
    hex_code = h.hexdigest()[:16].upper()
    return '-'.join([hex_code[i:i+4] for i in range(0, len(hex_code), 4)])

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python3 generate_activation_code.py <APP_ID>")
        sys.exit(1)
        
    app_id = sys.argv[1]
    secret = get_secret()
    code = generate_code(app_id, secret)
    print(f"Activation Code for App ID '{app_id}': {code}")
