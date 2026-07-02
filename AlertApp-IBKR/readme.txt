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
STOP THE LISTENER
--------------------------------------------------------------------------------

  Close the CMD window, or Ctrl+C, or end the python.exe process in Task Manager.

================================================================================
