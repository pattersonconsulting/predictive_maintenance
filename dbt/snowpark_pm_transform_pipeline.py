# Snowpark for Python
from snowflake.snowpark.session import Session
from snowflake.snowpark.types import IntegerType, StringType, StructType, FloatType, StructField, DateType, Variant
from snowflake.snowpark.functions import udf, sum, col,array_construct,month,year,call_udf,lit
from  snowflake.snowpark.functions import col, when, count


from snowflake.snowpark.version import VERSION
# Misc
import json
import pandas as pd
import logging 
logger = logging.getLogger("snowflake.snowpark.session")
logger.setLevel(logging.ERROR)

import pandas as pd
import numpy as np

# SNOWFLAKE CONNECTION

# Question: how are we going to manage connection strings with service accounts?

connection_parameters = json.load(open('jp_connection.json'))




# Create Snowflake Session object

session = Session.builder.configs(connection_parameters).create()
session.sql_simplifier_enabled = True

snowflake_environment = session.sql('select current_user(), current_role(), current_database(), current_schema(), current_version(), current_warehouse()').collect()
snowpark_version = VERSION

# Current Environment Details
print('User                        : {}'.format(snowflake_environment[0][0]))
print('Role                        : {}'.format(snowflake_environment[0][1]))
print('Database                    : {}'.format(snowflake_environment[0][2]))
print('Schema                      : {}'.format(snowflake_environment[0][3]))
print('Warehouse                   : {}'.format(snowflake_environment[0][5]))
print('Snowflake version           : {}'.format(snowflake_environment[0][4]))
print('Snowpark for Python version : {}.{}.{}'.format(snowpark_version[0],snowpark_version[1],snowpark_version[2]))






# start working with Snowpark

df_table = session.table("SUMMARY_SENSOR_DATA")


# now create a new column ("TYPE_NUM") based on the value of another column (TYPE)
df_type_converted = df_table.withColumn( "TYPE_NUM", ( 
    when(col("TYPE") == "H", lit(2))
    .when(col("TYPE") == "M", lit(1))
    .when(col("TYPE") == "L", lit(0))
    )
    )

df_eda = df_type_converted.drop("TYPE", "TWF", "HDF", "PWF", "OSF", "RNF", "PRODUCT_ID", "UDI")






# Save features into a Snowflake table call MARKETING_BUDGETS_FEATURES
df_eda.write.mode('overwrite').save_as_table('SENSOR_DATA_FEATURES')

# close the session
session.close()