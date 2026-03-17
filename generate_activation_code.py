
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

def generate_qr(app_id, code):
    try:
        import qrcode
        qr = qrcode.QRCode(
            version=1,
            error_correction=qrcode.constants.ERROR_CORRECT_L,
            box_size=10,
            border=4,
        )
        qr.add_data(code)
        qr.make(fit=True)

        img = qr.make_image(fill_color="black", back_color="white")
        filename = f"activation_qr_{app_id.replace(':', '_')}.png"
        img.save(filename)
        
        # Also print to terminal
        print("\nScan this QR code to activate:")
        qr.print_ascii()
        
        return filename
    except ImportError:
        return None

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python3 generate_activation_code.py <APP_ID>")
        sys.exit(1)
        
    app_id = sys.argv[1]
    secret = get_secret()
    code = generate_code(app_id, secret)
    print(f"Activation Code for App ID '{app_id}': {code}")
    
    qr_file = generate_qr(app_id, code)
    if qr_file:
        print(f"QR Code generated: {qr_file}")
    else:
        print("\nNote: 'qrcode' library not found. To generate QR codes, install it via:")
        print("pip install qrcode[pil]")
