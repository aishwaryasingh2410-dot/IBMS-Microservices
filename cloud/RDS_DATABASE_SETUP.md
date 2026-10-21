# AWS RDS database setup

RDS is created with `auth_db` by Terraform. Because one RDS instance can host multiple logical databases, create the remaining IBMS databases after the instance is available:

```sql
CREATE DATABASE IF NOT EXISTS transaction_db;
CREATE DATABASE IF NOT EXISTS notification_db;
CREATE DATABASE IF NOT EXISTS loan_db;
```

You can run these statements from a MySQL client connected to the private RDS endpoint.

The application services already use `DB_NAME` separately, so each service continues to own its logical database even though the databases reside on the managed RDS instance.
