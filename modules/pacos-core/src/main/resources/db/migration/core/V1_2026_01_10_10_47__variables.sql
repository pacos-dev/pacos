INSERT INTO PUBLIC.APP_SYSTEM_VARIABLE(ID, NAME, DESCRIPTION, JS)
VALUES ((NEXT VALUE FOR SYSTEM_VARIABLE_SEQ), '$uuid', 'Random UUID v4 (Vanilla JS)',
        'return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, function(c) { var r = Math.random() * 16 | 0, v = c == "x" ? r : (r & 0x3 | 0x8); return v.toString(16); });');
INSERT INTO PUBLIC.APP_SYSTEM_VARIABLE(ID, NAME, DESCRIPTION, JS)
VALUES ((NEXT VALUE FOR SYSTEM_VARIABLE_SEQ), '$random_email', 'Random email address',
        'return "user_" + Math.random().toString(36).substring(2, 10) + "@example.com";');
INSERT INTO PUBLIC.APP_SYSTEM_VARIABLE(ID, NAME, DESCRIPTION, JS)
VALUES ((NEXT VALUE FOR SYSTEM_VARIABLE_SEQ), '$random_string', 'Random 8-character string',
        'return Math.random().toString(36).substring(2, 10);');
INSERT INTO PUBLIC.APP_SYSTEM_VARIABLE(ID, NAME, DESCRIPTION, JS)
VALUES ((NEXT VALUE FOR SYSTEM_VARIABLE_SEQ), '$random_bool', 'Random boolean value', 'return Math.random() >= 0.5;');

INSERT INTO PUBLIC.APP_SYSTEM_VARIABLE(ID, NAME, DESCRIPTION, JS)
VALUES ((NEXT VALUE FOR SYSTEM_VARIABLE_SEQ), '$timestamp', 'Current Unix timestamp in seconds',
        'return Math.floor(Date.now() / 1000).toString();');
INSERT INTO PUBLIC.APP_SYSTEM_VARIABLE(ID, NAME, DESCRIPTION, JS)
VALUES ((NEXT VALUE FOR SYSTEM_VARIABLE_SEQ), '$iso_date', 'Current date in ISO 8601 format',
        'return new Date().toISOString();');

INSERT INTO PUBLIC.APP_SYSTEM_VARIABLE(ID, NAME, DESCRIPTION, JS)
VALUES ((NEXT VALUE FOR SYSTEM_VARIABLE_SEQ), '$protocol', 'Connection protocol', 'return "http";');
INSERT INTO PUBLIC.APP_SYSTEM_VARIABLE(ID, NAME, DESCRIPTION, JS)
VALUES ((NEXT VALUE FOR SYSTEM_VARIABLE_SEQ), '$base_url', 'Base URL for API',
        'return "http://localhost:8080/api/v1";');
INSERT INTO PUBLIC.APP_SYSTEM_VARIABLE(ID, NAME, DESCRIPTION, JS)
VALUES ((NEXT VALUE FOR SYSTEM_VARIABLE_SEQ), '$random_ip', 'Random IPv4 address',
        'return Array.from({length: 4}, () => Math.floor(Math.random() * 256)).join(".");');

INSERT INTO PUBLIC.APP_SYSTEM_VARIABLE(ID, NAME, DESCRIPTION, JS)
VALUES ((NEXT VALUE FOR SYSTEM_VARIABLE_SEQ), '$test_token', 'Mock JWT token for testing',
        'return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.e30.Et92AyWnnevept_S6unW71S67667Sdf6s";');
INSERT INTO PUBLIC.APP_SYSTEM_VARIABLE(ID, NAME, DESCRIPTION, JS)
VALUES ((NEXT VALUE FOR SYSTEM_VARIABLE_SEQ), '$basic_auth', 'Base64 encoded admin:admin (Static)',
        'return "YWRtaW46YWRtaW4=";');

INSERT INTO PUBLIC.APP_SYSTEM_VARIABLE(ID, NAME, DESCRIPTION, JS)
VALUES ((NEXT VALUE FOR SYSTEM_VARIABLE_SEQ), '$request_id', 'Unique request identifier based on timestamp',
        'return "REQ-" + Date.now() + "-" + Math.floor(Math.random() * 1000);');
INSERT INTO PUBLIC.APP_SYSTEM_VARIABLE(ID, NAME, DESCRIPTION, JS)
VALUES ((NEXT VALUE FOR SYSTEM_VARIABLE_SEQ), '$random_name', 'Random first name from a predefined list',
        'const names = ["Alice", "Bob", "Charlie", "David", "Eve"]; return names[Math.floor(Math.random() * names.length)];');
INSERT INTO PUBLIC.APP_SYSTEM_VARIABLE(ID, NAME, DESCRIPTION, JS)
VALUES ((NEXT VALUE FOR SYSTEM_VARIABLE_SEQ), '$random_phone', 'Random 9-digit phone number',
        'return Math.floor(100000000 + Math.random() * 900000000).toString();');
INSERT INTO PUBLIC.APP_SYSTEM_VARIABLE(ID, NAME, DESCRIPTION, JS)
VALUES ((NEXT VALUE FOR SYSTEM_VARIABLE_SEQ), '$random_price', 'Random price between 10.00 and 1000.00',
        'return (Math.random() * (1000 - 10) + 10).toFixed(2);');
INSERT INTO PUBLIC.APP_SYSTEM_VARIABLE(ID, NAME, DESCRIPTION, JS)
VALUES ((NEXT VALUE FOR SYSTEM_VARIABLE_SEQ), '$random_color', 'Random HEX color code',
        'return "#" + Math.floor(Math.random()*16777215).toString(16).padStart(6, "0");');
