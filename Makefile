.PHONY: init up jar distbin distres dist

SVN=http://10.163.254.253/svn/viking/trunk
DISTTO=/qzdata1/server/game1
LIBS=../release/server/libs
RES=../release/server/assets
JAVARES= world/gen/game/worldsrv/config

DISTTO0=/qzdata1/server/game0

up:
	svn up --force ../serverNew ../configNew ../msgNew

res:
	svn up --force ../configNew && \
	cd ../configNew/gen && sh excel2conf.sh && cd - && \
	svn add assets/json/* --force && \
	svn add ${RES}/json* --force && \
	svn add ${JAVARES}/* --force && \
	svn ci assets/json ${RES}/json ${JAVARES} -m "build"

bin:
	svn up --force ../serverNew ../msgNew && \
	svn up --force ${LIBS} && \
	ant -f msg/build.xml && \
	ant -f core/build.xml && \
	ant -f platform/build.xml && \
	ant -f world/build.xml && \
	svn ci libs ${LIBS} -m "build"

__dist:
	mkdir __dist

define _distbin
	svn up --force ${LIBS} && \
	cp ${LIBS}/game-core.jar 		__dist/ && \
	cp ${LIBS}/game-msg.jar 		__dist/ && \
	cp ${LIBS}/game-world.jar 		__dist/ && \
	cp ${LIBS}/game-platform.jar 	__dist/ && \
	scp foot viki:${1} && \
	cd __dist && \
	tar -zcf libs.tar *.jar && \
	scp libs.tar viki:${1} && \
	ssh viki "cd ${1} && tar -zxf libs.tar -C libs"
endef

define _distres
	svn up --force ${RES} && \
	cp -r ${RES} __dist && cd __dist && \
	tar -zcf res.tar assets && \
	scp res.tar viki:${1} && \
	ssh viki "cd ${1} && tar -zxf res.tar"
endef

distbin: | __dist
	$(call _distbin,${DISTTO})

distres: | __dist
	$(call _distres,${DISTTO})

dist: distres distbin

distbin0: | __dist
	$(call _distbin,${DISTTO0})

distres0: | __dist
	$(call _distres,${DISTTO0})

dist0: distres0 distbin0
