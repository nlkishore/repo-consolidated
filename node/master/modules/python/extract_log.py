import paramiko
import sys
from datetime import datetime, timedelta

def extract_log_between_times(host, username, password, log_path, start_time, end_time):
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(host, username=username, password=password)
    # Format for grep: 'YYYY-MM-DD HH:MM:SS'
    start_str = start_time.strftime('%Y-%m-%d %H:%M:%S')
    end_str = end_time.strftime('%Y-%m-%d %H:%M:%S')
    # Adjust grep command as per log format (assuming timestamp at line start)
    grep_cmd = f"awk '$0 >= \"{start_str}\" && $0 <= \"{end_str}\"' {log_path}"
    stdin, stdout, stderr = ssh.exec_command(grep_cmd)
    logs = stdout.read().decode()
    ssh.close()
    return logs

def extract_log_by_session_id(host, username, password, log_path, session_id):
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(host, username=username, password=password)
    # Get all lines containing session_id
    grep_cmd = f"grep -n '{session_id}' {log_path}"
    stdin, stdout, stderr = ssh.exec_command(grep_cmd)
    lines = stdout.read().decode().splitlines()
    if not lines:
        ssh.close()
        return f"Session ID '{session_id}' not found in log."
    # Extract line numbers of first and last occurrence
    first_line = int(lines[0].split(':')[0])
    last_line = int(lines[-1].split(':')[0])
    # Extract lines between first and last occurrence (inclusive)
    sed_cmd = f"sed -n '{first_line},{last_line}p' {log_path}"
    stdin, stdout, stderr = ssh.exec_command(sed_cmd)
    log_block = stdout.read().decode()
    ssh.close()
    return log_block

def extract_api_requests_responses(host, username, password, log_path, start_time, end_time):
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(host, username=username, password=password)
    start_str = start_time.strftime('%Y-%m-%d %H:%M:%S')
    end_str = end_time.strftime('%Y-%m-%d %H:%M:%S')
    # Extract lines in time range
    awk_cmd = f"awk '$0 >= \"{start_str}\" && $0 <= \"{end_str}\"' {log_path}"
    stdin, stdout, stderr = ssh.exec_command(awk_cmd)
    lines = stdout.read().decode().splitlines()
    # Filter for API request/response lines (customize patterns as needed)
    api_reqs = [l for l in lines if 'API Request' in l]
    api_resps = [l for l in lines if 'API Response' in l]
    ssh.close()
    return {'requests': api_reqs, 'responses': api_resps}
import os
def find_files_with_string(root_dir, search_string):
    matches = []
    for dirpath, dirnames, filenames in os.walk(root_dir):
        for filename in filenames:
            file_path = os.path.join(dirpath, filename)
            try:
                with open(file_path, 'r', errors='ignore') as f:
                    for line in f:
                        if search_string in line:
                            matches.append(file_path)
                            break  # Stop after first match in this file
            except Exception as e:
                # Skip files that can't be read (e.g., binaries)
                continue
    return matches

if __name__ == "__main__":
    # Example usage: python extract_log.py <host> <username> <password> <log_path> <start_time> <end_time>
    if len(sys.argv) != 7 and len(sys.argv) != 8 and len(sys.argv) != 9:
        print("Usage: python extract_log.py <host> <username> <password> <log_path> <start_time> <end_time> [--session <session_id>] [--api]")
        print("Time format: YYYY-MM-DD HH:MM:SS")
        sys.exit(1)
    host, username, password, log_path, start_str, end_str = sys.argv[1:7]
    start_time = datetime.strptime(start_str, '%Y-%m-%d %H:%M:%S')
    end_time = datetime.strptime(end_str, '%Y-%m-%d %H:%M:%S')
    if len(sys.argv) == 9 and sys.argv[7] == '--api':
        # python extract_log.py <host> <username> <password> <log_path> <start_time> <end_time> --api <pattern>
        api_result = extract_api_requests_responses(host, username, password, log_path, start_time, end_time)
        print('API Requests:')
        for req in api_result['requests']:
            print(req)
        print('\nAPI Responses:')
        for resp in api_result['responses']:
            print(resp)
    elif len(sys.argv) == 8 and sys.argv[7] == '--session':
        session_id = sys.argv[6]
        logs = extract_log_by_session_id(host, username, password, log_path, session_id)
        print(logs)
    else:
        logs = extract_log_between_times(host, username, password, log_path, start_time, end_time)
        print(logs)
    # Example usage:
    # files = find_files_with_string('/path/to/search', 'your_search_string')
    # for f in files:
    #     print(f)
