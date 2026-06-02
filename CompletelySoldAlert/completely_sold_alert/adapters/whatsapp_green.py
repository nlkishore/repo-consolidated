"""Green API WhatsApp sender (AlertApp pattern)."""

from __future__ import annotations

import logging

from completely_sold_alert.config import AppSettings

logger = logging.getLogger("completely_sold_alert")


def send_whatsapp(settings: AppSettings, text: str, *, dry_run: bool = False) -> tuple[bool, str]:
    if dry_run:
        logger.info("DRY-RUN WhatsApp message (%d chars)", len(text))
        return True, "dry_run"

    wa = settings.whatsapp
    if not wa.id_instance or not wa.api_token or not wa.target_phone:
        return False, "WhatsApp credentials missing in settings.yaml or env"

    try:
        from whatsapp_api_client_python import API
    except ImportError:
        return False, "Install: pip install whatsapp-api-client-python"

    green_api = API.GreenAPI(wa.id_instance, wa.api_token)
    chat_id = f"{wa.target_phone}@c.us"
    response = green_api.sending.sendMessage(chat_id, text)
    if response.code == 200:
        return True, "sent"
    return False, getattr(response, "error", None) or f"code {response.code}"
