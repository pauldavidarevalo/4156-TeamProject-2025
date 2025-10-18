import random
import time
from datetime import datetime

def generate_log():
    """Generate one fake Apache-style access log line."""
    methods = ["GET", "POST", "PUT", "DELETE"]
    endpoints = ["/", "/login", "/api/data", "/users", "/images/logo.png"]
    status_codes = [200, 301, 400, 403, 404, 500]
    user_agents = [
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)",
        "Mozilla/5.0 (X11; Linux x86_64)",
        "curl/8.0.1",
        "PostmanRuntime/7.32.0"
    ]

    ip = ".".join(str(random.randint(0, 255)) for _ in range(4))
    timestamp = datetime.now().strftime("%d/%b/%Y:%H:%M:%S %z")
    method = random.choice(methods)
    endpoint = random.choice(endpoints)
    status = random.choice(status_codes)
    bytes_sent = random.randint(200, 5000)
    referrer = "-"
    user_agent = random.choice(user_agents)

    return f'{ip} - - [{timestamp}] "{method} {endpoint} HTTP/1.1" {status} {bytes_sent} "{referrer}" "{user_agent}"'

def main():
    filename = "sampleApacheLog2.log"
    print(f"Writing logs to {filename} — press Ctrl+C to stop.")

    with open(filename, "a") as f:
        while True:
            log_line = generate_log()
            f.write(log_line + "\n")
            f.flush()  # ensures immediate write to disk
            print(log_line)
            time.sleep(random.uniform(0.5, 2.0))  # wait 0.5–2 seconds between logs

if __name__ == "__main__":
    main()
