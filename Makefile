ifdef update
	u=-u
endif

export GO111MODULE=on

.PHONY: deps
deps:
	lein deps
	lein install

.PHONY: test
test: deps
	docker-compose up -d
	go test -v -cover ./...

.PHONY: install
install: deps
	lein uberjar
