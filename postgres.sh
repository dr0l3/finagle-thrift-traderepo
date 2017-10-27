#!/usr/bin/env bash
docker run --name pg-scratchpad -e POSTGRES_PASSWORD=world -p 5432:5432 -v /var/lib/postgresql/data -d postgres