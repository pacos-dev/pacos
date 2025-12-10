DELETE FROM PUBLIC.PUBLIC.APP_REGISTRY WHERE PUBLIC.PUBLIC.APP_REGISTRY.REGISTRY_NAME='DEFAULT_DOCK_CONFIG';

INSERT INTO PUBLIC.PUBLIC.APP_REGISTRY(REGISTRY_NAME, VALUE) VALUES ('DEFAULT_DOCK_CONFIG','{"activators":["PanelVariable"]}');

INSERT INTO PUBLIC.PUBLIC.APP_SYSTEM_VARIABLE(NAME,DESCRIPTION,JS) VALUES ('$host','Host machine','return ''localhost''');
INSERT INTO PUBLIC.PUBLIC.APP_SYSTEM_VARIABLE(NAME,DESCRIPTION,JS) VALUES ('$port','Port machine','return ''8080''');
INSERT INTO PUBLIC.PUBLIC.APP_SYSTEM_VARIABLE(NAME,DESCRIPTION,JS) VALUES ('$current_date','current date in format yyy-mm-dd','return new Date().toLocaleDateString();');
INSERT INTO PUBLIC.PUBLIC.APP_SYSTEM_VARIABLE(NAME,DESCRIPTION,JS) VALUES ('$current_time','current time in format hh:mm:ss','return new Date().toLocaleTimeString();');
INSERT INTO PUBLIC.PUBLIC.APP_SYSTEM_VARIABLE(NAME,DESCRIPTION,JS) VALUES ('$random_number','random number from 1 to 10','return  Math.floor(Math.random() * 10) + 1;');
INSERT INTO PUBLIC.PUBLIC.APP_SYSTEM_VARIABLE(NAME,DESCRIPTION,JS) VALUES ('$random_letter','random letter from A to Z','const letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    const randomIndex = Math.floor(Math.random() * letters.length);
    return letters[randomIndex];');
