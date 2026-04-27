import unittest
import requests

BASE_URL = "http://localhost:3000"

class TestMyWebAppAPI(unittest.TestCase):

    def test_root_api(self):
        response = requests.get(f"{BASE_URL}/api")
        self.assertEqual(response.status_code, 200)
        self.assertIn("message", response.json())
        self.assertEqual(response.json()["message"], "Hello, world!")

    def test_invalid_endpoint(self):
        response = requests.get(f"{BASE_URL}/api/unknown")
        self.assertEqual(response.status_code, 404)

    def test_healthcheck(self):
        response = requests.get(f"{BASE_URL}/health")
        # This route is optional — add in Express if you want
        self.assertIn(response.status_code, [200, 404])

if __name__ == "__main__":
    unittest.main()