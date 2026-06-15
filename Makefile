.PHONY: start stop dev dev-backend dev-frontend dev-stop help

include .env
export

help:
	@echo ""
	@echo "  Vinyl Exchange — available commands"
	@echo "  ──────────────────────────────────────────────"
	@echo "  make start         Full stack in Docker (demo)"
	@echo "  make stop          Stop full stack"
	@echo "  make dev           Start infrastructure only"
	@echo "  make dev-backend   Run backend locally"
	@echo "  make dev-frontend  Run frontend locally"
	@echo "  make dev-stop      Stop dev infrastructure"
	@echo "  ──────────────────────────────────────────────"
	@echo ""

start:
	docker compose up --build -d
	@echo "Waiting for backend to be ready..."
	@until docker inspect -f '{{.State.Health.Status}}' vx-backend 2>/dev/null | grep -q "healthy"; do sleep 2; done
	@echo "Waiting for frontend to be ready..."
	@until curl -s -o /dev/null -w "%{http_code}" http://localhost:80 | grep -q "200"; do sleep 2; done
	@echo ""
	@echo "==============================="
	@echo "  Vinyl Exchange is running"
	@echo "==============================="
	@echo "  Frontend : http://localhost:80"
	@echo "  Backend  : http://localhost:$(SERVER_PORT)"
	@echo "==============================="

stop:
	docker compose down

dev:
	docker compose -f docker-compose.infra.yml up -d
	@echo "Waiting for Postgres..."
	@until docker inspect -f '{{.State.Health.Status}}' postgres-local 2>/dev/null | grep -q "healthy"; do sleep 2; done
	@echo ""
	@echo "==============================="
	@echo "  Infrastructure ready"
	@echo "==============================="
	@echo "  Run in separate terminals:"
	@echo "  make dev-backend"
	@echo "  make dev-frontend"
	@echo "==============================="

dev-backend:
	cd backend && DB_HOST=localhost OPENSEARCH_HOST=localhost mvn spring-boot:run

dev-frontend:
	cd frontend && npm run dev

dev-stop:
	docker compose -f docker-compose.infra.yml down
	@echo "Dev infrastructure stopped."