DELETE from offence_schedule_part;
DELETE from schedule_part;
DELETE from schedule;
DELETE from offence;
DELETE from sdrs_load_result_history;

UPDATE sdrs_load_result SET status = NULL, load_type = NULL, load_date = NULL, last_successful_load_date = NULL;
UPDATE feature_toggle set enabled = true;

