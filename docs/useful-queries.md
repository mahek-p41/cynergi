# Useful Queries

Some queries that will make your life easier :)

## User queries
```sql
select emp_number, emp_pass_code, comp_dataset_code
from system_employees_vw
where comp_dataset_code = 'corrto' -- change this to whatever dataset you need
  and store_number = 1 -- change this to whatever store number you are trying to log into
order by emp_number
;
```
