use database PREDICTIVE_MAINTENANCE;

COPY INTO summary_sensor_data
From @%summary_sensor_data
FILE_FORMAT = ( TYPE = CSV, SKIP_HEADER=1 )
