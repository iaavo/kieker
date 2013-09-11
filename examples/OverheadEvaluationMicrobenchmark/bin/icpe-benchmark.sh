#!/bin/bash

#SUDOCMD="pfexec"
SUDOCMD=""
#BINDJAVA="${SUDOCMD} psrset -e 1"
BINDJAVA="/localhome/ffi/jdk1.7.0_25/bin/"

BINDIR=bin/
BASEDIR=

SLEEPTIME=30            ## 30
NUM_LOOPS=10            ## 10
THREADS=1               ## 1
MAXRECURSIONDEPTH=10    ## 10
TOTALCALLS=20000000     ## 2000000
METHODTIME=1            ## 500000

TIME=`expr ${METHODTIME} \* ${TOTALCALLS} / 1000000000 \* 4 \* ${MAXRECURSIONDEPTH} \* ${NUM_LOOPS} + ${SLEEPTIME} \* 4 \* ${NUM_LOOPS}  \* ${MAXRECURSIONDEPTH}`
echo "Experiment will take circa ${TIME} seconds."

# determine correct classpath separator
CPSEPCHAR=":" # default :, ; for windows
if [ ! -z "$(uname | grep -i WIN)" ]; then CPSEPCHAR=";"; fi
# echo "Classpath separator: '${CPSEPCHAR}'"

RESULTSDIR="${BASEDIR}tmp/results-benchmark-recursive/"
echo "Removing and recreating '$RESULTSDIR'"
(${SUDOCMD} rm -rf ${RESULTSDIR}) && mkdir ${RESULTSDIR}
mkdir ${RESULTSDIR}stat/

# Clear kieker.log and initialize logging
rm -f ${BASEDIR}kieker.log
touch ${BASEDIR}kieker.log

RESULTSFN="${RESULTSDIR}results"

JAVAARGS="-server"
JAVAARGS="${JAVAARGS} -d64"
JAVAARGS="${JAVAARGS} -Xms1G -Xmx4G"
JAVAARGS="${JAVAARGS} -verbose:gc -XX:+PrintCompilation"
#JAVAARGS="${JAVAARGS} -XX:+PrintInlining"
#JAVAARGS="${JAVAARGS} -XX:+UnlockDiagnosticVMOptions -XX:+LogCompilation"
#JAVAARGS="${JAVAARGS} -Djava.compiler=NONE"
JAR="-jar dist/OverheadEvaluationMicrobenchmark.jar"

JAVAARGS_NOINSTR="${JAVAARGS}"
JAVAARGS_LTW="${JAVAARGS} -javaagent:${BASEDIR}lib/kieker-1.8-SNAPSHOT_aspectj.jar -Dorg.aspectj.weaver.showWeaveInfo=false -Daj.weaving.verbose=false"
JAVAARGS_KIEKER_DEACTV="${JAVAARGS_LTW} -Dkieker.monitoring.adaptiveMonitoring.configFile=META-INF/kieker.monitoring.adaptiveMonitoring.disabled.conf -Dkieker.monitoring.writer=kieker.monitoring.writer.DummyWriter"
JAVAARGS_KIEKER_NOLOGGING="${JAVAARGS_LTW} -Dkieker.monitoring.writer=kieker.monitoring.writer.DummyWriter"
#JAVAARGS_KIEKER_LOGGING="${JAVAARGS_LTW} -Dkieker.monitoring.writer=kieker.monitoring.writer.filesystem.AsyncFsWriter -Dkieker.monitoring.writer.filesystem.AsyncFsWriter.storeInJavaIoTmpdir=false -Dkieker.monitoring.writer.filesystem.AsyncFsWriter.customStoragePath=${BASEDIR}tmp"
JAVAARGS_KIEKER_LOGGING="${JAVAARGS_LTW} -Dkieker.monitoring.writer=kieker.monitoring.writer.filesystem.AsyncBinaryFsWriter -Dkieker.monitoring.writer.filesystem.AsyncBinaryFsWriter.storeInJavaIoTmpdir=false -Dkieker.monitoring.writer.filesystem.AsyncBinaryFsWriter.customStoragePath=${BASEDIR}tmp -Dkieker.monitoring.writer.filesystem.AsyncBinaryFsWriter.QueueFullBehavior=1 -Dkieker.monitoring.writer.filesystem.AsyncBinaryFsWriter.QueueSize=1000000 -Dkieker.monitoring.writer.filesystem.AsyncBinaryFsWriter.bufferSize=16777216"

## Write configuration
uname -a >${RESULTSDIR}configuration.txt
${BINDJAVA}java ${JAVAARGS} -version 2>>${RESULTSDIR}configuration.txt
echo "JAVAARGS: ${JAVAARGS}" >>${RESULTSDIR}configuration.txt
echo "" >>${RESULTSDIR}configuration.txt
echo "Runtime: circa ${TIME} seconds" >>${RESULTSDIR}configuration.txt
echo "" >>${RESULTSDIR}configuration.txt
echo "SLEEPTIME=${SLEEPTIME}" >>${RESULTSDIR}configuration.txt
echo "NUM_LOOPS=${NUM_LOOPS}" >>${RESULTSDIR}configuration.txt
echo "TOTALCALLS=${TOTALCALLS}" >>${RESULTSDIR}configuration.txt
echo "METHODTIME=${METHODTIME}" >>${RESULTSDIR}configuration.txt
echo "THREADS=${THREADS}" >>${RESULTSDIR}configuration.txt
echo "MAXRECURSIONDEPTH=${MAXRECURSIONDEPTH}" >>${RESULTSDIR}configuration.txt
sync

## Execute Benchmark

for ((i=1;i<=${NUM_LOOPS};i+=1)); do
    echo "## Starting iteration ${i}/${NUM_LOOPS}"

        # 1 No instrumentation
        echo " # ${i}.1 No instrumentation"
        mpstat 1 > ${RESULTSDIR}stat/mpstat-${i}-1-1.txt &
        vmstat 1 > ${RESULTSDIR}stat/vmstat-${i}-1-1.txt &
        iostat -xn 10 > ${RESULTSDIR}stat/iostat-${i}-1-1.txt &
        ${BINDJAVA}java  ${JAVAARGS_NOINSTR} ${JAR} \
            --output-filename ${RESULTSFN}-${i}-1-1.csv \
            --totalcalls ${TOTALCALLS} \
            --methodtime ${METHODTIME} \
            --totalthreads ${THREADS} \
            --recursiondepth ${MAXRECURSIONDEPTH}
        kill %mpstat
        kill %vmstat
        kill %iostat
        [ -f ${BASEDIR}hotspot.log ] && mv ${BASEDIR}hotspot.log ${RESULTSDIR}hotspot-${i}-1-1.log
        sync
        sleep ${SLEEPTIME}

        # 2 Deactivated probe
        echo " # ${i}.2 Deactivated probe"
        mpstat 1 > ${RESULTSDIR}stat/mpstat-${i}-1-2.txt &
        vmstat 1 > ${RESULTSDIR}stat/vmstat-${i}-1-2.txt &
        iostat -xn 10 > ${RESULTSDIR}stat/iostat-${i}-1-2.txt &
        ${BINDJAVA}java  ${JAVAARGS_KIEKER_DEACTV} ${JAR} \
            --output-filename ${RESULTSFN}-${i}-1-2.csv \
            --totalcalls ${TOTALCALLS} \
            --methodtime ${METHODTIME} \
            --totalthreads ${THREADS} \
            --recursiondepth ${MAXRECURSIONDEPTH}
        kill %mpstat
        kill %vmstat
        kill %iostat
        [ -f ${BASEDIR}hotspot.log ] && mv ${BASEDIR}hotspot.log ${RESULTSDIR}hotspot-${i}-1-2.log
        echo >>${BASEDIR}kieker.log
        echo >>${BASEDIR}kieker.log
        sync
        sleep ${SLEEPTIME}

        # 3 No logging
        echo " # ${i}.3 No logging (null writer)"
        mpstat 1 > ${RESULTSDIR}stat/mpstat-${i}-1-3.txt &
        vmstat 1 > ${RESULTSDIR}stat/vmstat-${i}-1-3.txt &
        iostat -xn 10 > ${RESULTSDIR}stat/iostat-${i}-1-3.txt &
        ${BINDJAVA}java  ${JAVAARGS_KIEKER_NOLOGGING} ${JAR} \
            --output-filename ${RESULTSFN}-${i}-1-3.csv \
            --totalcalls ${TOTALCALLS} \
            --methodtime ${METHODTIME} \
            --totalthreads ${THREADS} \
            --recursiondepth 1
        kill %mpstat
        kill %vmstat
        kill %iostat
        [ -f ${BASEDIR}hotspot.log ] && mv ${BASEDIR}hotspot.log ${RESULTSDIR}hotspot-${i}-1-3.log
        echo >>${BASEDIR}kieker.log
        echo >>${BASEDIR}kieker.log
        sync
        sleep ${SLEEPTIME}

        # 4 Logging
        echo " # ${i}.4 Logging"
        mpstat 1 > ${RESULTSDIR}stat/mpstat-${i}-1-4.txt &
        vmstat 1 > ${RESULTSDIR}stat/vmstat-${i}-1-4.txt &
        iostat -xn 10 > ${RESULTSDIR}stat/iostat-${i}-1-4.txt &
        ${BINDJAVA}java  ${JAVAARGS_KIEKER_LOGGING} ${JAR} \
            --output-filename ${RESULTSFN}-${i}-1-4.csv \
            --totalcalls ${TOTALCALLS} \
            --methodtime ${METHODTIME} \
            --totalthreads ${THREADS} \
            --recursiondepth ${MAXRECURSIONDEPTH}
        kill %mpstat
        kill %vmstat
        kill %iostat
        # mkdir -p ${RESULTSDIR}kiekerlog/
        # mv ${BASEDIR}tmp/kieker-* ${RESULTSDIR}kiekerlog/
        ${SUDOCMD} rm -rf ${BASEDIR}tmp/kieker-*
        [ -f ${BASEDIR}hotspot.log ] && mv ${BASEDIR}hotspot.log ${RESULTSDIR}hotspot-${i}-1-4.log
        echo >>${BASEDIR}kieker.log
        echo >>${BASEDIR}kieker.log
        sync
        sleep ${SLEEPTIME}

done
#tar cf ${RESULTSDIR}kiekerlog.tar ${RESULTSDIR}kiekerlog
#${SUDOCMD} rm -rf ${RESULTSDIR}kiekerlog/
#gzip -9 ${RESULTSDIR}kiekerlog.tar
tar cf ${RESULTSDIR}stat.tar ${RESULTSDIR}stat
rm -rf ${RESULTSDIR}stat/
gzip -9 ${RESULTSDIR}stat.tar
mv ${BASEDIR}kieker.log ${RESULTSDIR}kieker.log
[ -f ${RESULTSDIR}hotspot-1-1-1.log ] && grep "<task " ${RESULTSDIR}hotspot-*.log >${RESULTSDIR}log.log
[ -f ${BASEDIR}nohup.out ] && mv ${BASEDIR}nohup.out ${RESULTSDIR}
[ -f ${BASEDIR}errorlog.txt ] && mv ${BASEDIR}errorlog.txt ${RESULTSDIR}