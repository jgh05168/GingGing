import asyncio
from bleak import BleakClient
import cv2
import numpy as np
import base64

number = 0

async def connect_and_display_image(address, char_uuid):
    global number
    async with BleakClient(address) as client:
        if not await client.is_connected():
            print(f"Failed to connect to {address}")
            return

        print(f"Connected to {address}")

        while True:
            try:
                # Initialize buffer and packet ID set
                image_buffer = bytearray()
                received_packet_ids = set()

                while True:
                    # Read data from the BLE characteristic
                    part_data = await client.read_gatt_char(char_uuid)
                    if part_data:
                        print(part_data)
                        # Check if end of data stream
                        if part_data == b'0':
                            break
                        # Split header from data without decoding
                        header = part_data[:part_data.find(b'|')]
                        raw_data = part_data[part_data.find(b'|')+1:]
                        packet_id = int(header.decode())
                        if packet_id in received_packet_ids:
                            print(f"Duplicate packet ID {packet_id} received, skipping...")
                            continue  # Skip if packet ID was already processed

                        received_packet_ids.add(packet_id)  # Add new packet ID
                        image_buffer.extend(raw_data)  # Append raw data to buffer
                    else:
                        print("No more data received.")
                        break
                
                print(received_packet_ids)
                if image_buffer:
                    process_image(image_buffer)

            except Exception as e:
                print(f"Error: {e}")
                continue

def process_image(image_buffer):
    global number
    try:
        # image_decode = base64.b64decode(image_buffer)
        image_array = np.frombuffer(image_buffer, dtype=np.uint8)
        image = cv2.imdecode(image_array, cv2.IMREAD_COLOR)
        if image is not None:
            cv2.imshow("Received Image", image)
            cv2.waitKey(0)
            cv2.destroyAllWindows()
            cv2.imwrite(f"received_image{number + 1}.jpg", image)
            number += 1
            print("Image saved as 'received_image.jpg'")
        else:
            print("Failed to decode image data, possibly corrupted")
    except Exception as e:
        print(f"Error processing image: {e}")

if __name__ == "__main__":
    device_address = "0C:B8:15:F6:A7:5A"
    # device_address = "0C:B8:15:F4:CF:2A"
    characteristic_uuid = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"
    loop = asyncio.get_event_loop()
    # image_buffer = bytearray(b'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')
    # process_image(image_buffer)
    loop.run_until_complete(connect_and_display_image(device_address, characteristic_uuid))
