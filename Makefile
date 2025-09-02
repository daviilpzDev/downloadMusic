PY := python3
PIP := pip

.PHONY: help setup install dev fmt lint type test run docker-build docker-up docker-logs docker-down pre-commit

help:
	@echo "Targets: setup install dev fmt lint type test run docker-build docker-up docker-logs docker-down pre-commit"

setup:
	$(PY) -m venv venv
	. venv/bin/activate; $(PIP) install --upgrade pip

install:
	. venv/bin/activate; $(PIP) install -r requirements.txt

dev:
	. venv/bin/activate; $(PIP) install -r requirements.txt; $(PIP) install -e .[dev]

fmt:
	. venv/bin/activate; black src tests scripts --line-length 88

lint:
	. venv/bin/activate; flake8 src tests --max-line-length=88

type:
	. venv/bin/activate; mypy src

test:
	. venv/bin/activate; PYTHONPATH=src pytest -q

run:
	. venv/bin/activate; $(PY) -m youtube_watcher

docker-build:
	docker build -t youtube-watcher:latest .

docker-up:
	docker-compose up -d

docker-logs:
	docker-compose logs -f --tail=100

docker-down:
	docker-compose down

pre-commit:
	. venv/bin/activate; pre-commit install
	. venv/bin/activate; pre-commit run --all-files || true

