================================================================================
  AlertApp-IBKR — Green API WhatsApp Command Listener
================================================================================

Folder: C:\Investment\AlertApp-IBKR
Script: backgroundAlert.py

Listens for WhatsApp messages via Green API and responds to:
  STATUS              — confirms listener is online
  SUPPORT AAPL        — 3 recent pivot support levels (6-month Yahoo data)
  SOLD  or  SEND      — runs C:\Investment\CompletelySoldAlert\run-alert.bat,
                        which sends the "Completely Sold — Price Summary" digest

The listener reacts to BOTH incoming messages (from another phone) AND your own
OUTGOING messages (sent from the linked phone or the Green API web console), so
you can trigger commands from your own WhatsApp. It never replies "unknown
command" to outgoing messages, avoiding a reply loop.

Requires the Green API instance to be AUTHORIZED (scan QR at
https://console.green-api.com if state is notAuthorized).

--------------------------------------------------------------------------------
HOW TO RUN
--------------------------------------------------------------------------------

  cd C:\Investment\AlertApp-IBKR
  python backgroundAlert.py

  Or double-click / run:
  run-green-api-listener.bat

  Dependencies:
    pip install yfinance whatsapp-api-client-python pandas

--------------------------------------------------------------------------------
CREDENTIALS (pick one)
--------------------------------------------------------------------------------

  1. Environment variables:
       GREEN_API_ID_INSTANCE
       GREEN_API_TOKEN
       WHATSAPP_TARGET_PHONE   (no + sign)

  2. AutomatedTrading config (auto-loaded if present):
       C:\Investment\AutomatedTrading\config.ini
       [trading] whatsapp_id_instance, whatsapp_api_token_instance, whatsapp_target_phone

  3. Local config.ini in this folder with same keys.

--------------------------------------------------------------------------------
SINGLE INSTANCE ONLY (RACE-CONDITION GUARD)
--------------------------------------------------------------------------------

  Green API allows only ONE active receiveNotification consumer per instance.
  Running two listeners causes "consumer closed" (502 RMQ_ERROR) errors and
  dropped / delayed commands.

  This listener enforces a single instance using a Windows named mutex
  ("Global\AlertApp_IBKR_GreenAPI_Listener"). If you start it while another
  copy is already running, the new one prints:

    [X] Another AlertApp-IBKR listener is already running. Exiting...

  and exits immediately. The lock is released automatically by the OS when the
  running listener stops (no stale-lock cleanup needed).

  Check what is running:
    Get-CimInstance Win32_Process | Where-Object { $_.Name -like 'python*' -and
      $_.CommandLine -match 'backgroundAlert' } | Select-Object ProcessId, CreationDate

--------------------------------------------------------------------------------
STOP THE LISTENER
--------------------------------------------------------------------------------

  Close the CMD window, or Ctrl+C, or end the python.exe process in Task Manager.

================================================================================
