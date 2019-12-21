ifdef update
	u=-u
endif

export GO111MODULE=on

.PHONY: deps
deps:
	lein deps

.PHONY: test
test: deps
	docker-compose up -d
	lein test :all
