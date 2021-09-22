create or replace database predictive_maintenance;

create or replace table summary_sensor_data (
  UDI int ,
  Product_ID string ,
  Type string ,
  Air_temperature float ,
  Process_temperature float ,
  Rotational_speed float ,
  Torque float ,
  Tool_wear float ,

  Machine_failure int ,

  TWF int ,
  HDF int ,
  PWF int ,
  OSF int ,
  RNF int 
  
  );