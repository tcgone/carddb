######################### DEFINITIONS ############################

NAME=carddb

ifeq ($(RELEASE_VERSION),)
	RELEASE_VERSION  := $(shell mvn -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec)
endif
ifneq (,$(findstring -SNAPSHOT,$(RELEASE_VERSION)))
	RELEASE_VERSION_NSNP = $(shell echo $(RELEASE_VERSION) | perl -pe 's/-SNAPSHOT//')
else
	RELEASE_VERSION_NSNP = $(RELEASE_VERSION)
endif
ifeq ($(NEXT_VERSION),)
	NEXT_VERSION  := $(shell echo $(RELEASE_VERSION_NSNP) | perl -pe 's{^(([0-9]\.)+)?([0-9]+)$$}{$$1 . ($$3 + 1)}e')
endif
ifeq (,$(findstring -SNAPSHOT,$(NEXT_VERSION)))
	NEXT_VERSION_SNP = $(NEXT_VERSION)-SNAPSHOT
else
	NEXT_VERSION_SNP = $(NEXT_VERSION)
endif

GIT_TREE_STATE=$(shell (git status --porcelain | grep -q .) && echo dirty || echo clean)

######################## BUILD TARGETS ###########################

test:
	@echo $(GIT_TREE_STATE)

package:
	mvn clean package

version-release: git-check
	@echo setting release version: $(RELEASE_VERSION_NSNP)
	mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$(RELEASE_VERSION_NSNP)
	git add pom.xml
	git commit -m"[release] prepare $(NAME)-${RELEASE_VERSION_NSNP}"

version-next: git-check
	@echo setting next version: $(NEXT_VERSION_SNP)
	mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$(NEXT_VERSION_SNP)
	git add pom.xml
	git commit -m"[release] prepare for $(NAME)-${NEXT_VERSION_SNP}"

git-tag-release: git-check
	git tag "$(NAME)-${RELEASE_VERSION_NSNP}"

git-check:
ifeq ($(GIT_TREE_STATE),dirty)
	$(error git state is not clean)
endif

deploy:
	mvn deploy

release: git-check version-release git-tag-release package deploy version-next
