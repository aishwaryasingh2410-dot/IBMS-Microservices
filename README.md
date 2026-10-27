# IBMS project submission

The final college submission is in [`A004_Project/`](A004_Project/Project/README.md).

- `A004_Project/Project/src/` contains the complete four-service source, gateway, Compose files, environment examples, cloud configuration, and demo script.
- `A004_Project/Project/sql/` contains database creation, table DDL, and sample data scripts.
- `A004_Project/Documents/` contains the existing requirements/report documents and the presentation materials.

Run the local demo from the packaged source directory:

```sh
cd A004_Project/Project/src
docker compose up --build -d
./scripts/demo.sh
```

AWS remains a design-only path; Kubernetes is experimental and excluded from the submission. Do not commit credentials or real `.env` files.
