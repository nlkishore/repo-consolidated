# Convert Log4j2 API lines to JSONL (one object per line).
# Expected message fragment example:
#   status=200 service=payments-api latencyMs=45 endpoint=/api/v1/pay traceId=abc-123

BEGIN { FS=""; }

/^[0-9]{4}-[0-9]{2}-[0-9]{2} / {
  line = $0
  ts = ""
  level = ""
  traceId = ""
  status = ""
  service = ""
  latencyMs = ""
  endpoint = ""
  message = line

  if (match(line, /^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3}/)) {
    ts = substr(line, RSTART, RLENGTH)
  }
  if (match(line, / [A-Z]+ /)) {
    level = substr(line, RSTART + 1, RLENGTH - 2)
  }
  if (match(line, /traceId=([^ \]]+)/)) {
    traceId = substr(line, RSTART + 8, RLENGTH - 8)
  }
  if (match(line, /status=([0-9]+)/)) {
    status = substr(line, RSTART + 7, RLENGTH - 7)
  }
  if (match(line, /service=([^ ]+)/)) {
    service = substr(line, RSTART + 8, RLENGTH - 8)
  }
  if (match(line, /latencyMs=([0-9]+)/)) {
    latencyMs = substr(line, RSTART + 10, RLENGTH - 10)
  }
  if (match(line, /endpoint=([^ ]+)/)) {
    endpoint = substr(line, RSTART + 9, RLENGTH - 9)
  }

  if (status == "" && service == "") next

  gsub(/\\/, "\\\\", ts)
  gsub(/"/, "\\\"", ts)
  gsub(/\\/, "\\\\", level)
  gsub(/"/, "\\\"", level)
  gsub(/\\/, "\\\\", traceId)
  gsub(/"/, "\\\"", traceId)
  gsub(/\\/, "\\\\", service)
  gsub(/"/, "\\\"", service)
  gsub(/\\/, "\\\\", endpoint)
  gsub(/"/, "\\\"", endpoint)
  gsub(/\\/, "\\\\", message)
  gsub(/"/, "\\\"", message)

  printf("{\"@timestamp\":\"%s\",\"level\":\"%s\",\"traceId\":\"%s\",\"status\":%s,\"service\":\"%s\",\"latencyMs\":%s,\"endpoint\":\"%s\",\"message\":\"%s\",\"sourcetype\":\"log4j2\"}\n",
    ts, level, traceId,
    (status == "" ? "null" : status),
    service,
    (latencyMs == "" ? "null" : latencyMs),
    endpoint, message)
}
