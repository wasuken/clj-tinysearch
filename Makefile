ifdef update
	u=-u
endif

export GO111MODULE=on
export CUR_DIR=$(realpath ./)
export INDEX_DIR_PATH=$(CUR_DIR)/_index_data/
export SEARCH_INDEX_DIR_PATH=$(CUR_DIR)/testdata/index/
.PHONY: deps
deps:
	lein deps

.PHONY: test
test: deps
	docker-compose up -d
	lein test :all
