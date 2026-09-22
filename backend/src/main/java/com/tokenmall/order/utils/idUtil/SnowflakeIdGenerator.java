package com.tokenmall.order.utils.idUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 雪花算法ID生成器
 * 雪花算法生成的ID结构：
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 * 1位符号位 + 41位时间戳 + 5位数据中心ID + 5位机器ID + 12位序列号 = 64位
 * 特点：
 * - 生成的ID趋势递增
 * - 整个分布式系统内不会产生重复ID
 * - 能够根据时间戳排序
 * - 每毫秒能够生成4096个ID
 */
@Component
public class SnowflakeIdGenerator {

    /**
     * 起始时间戳 (2024-01-01 00:00:00)
     * 可以使用约69年
     */
    private static final long START_TIMESTAMP = 1704067200000L;

    /**
     * 数据中心ID位数
     */
    private static final long DATACENTER_ID_BITS = 5L;

    /**
     * 机器ID位数
     */
    private static final long MACHINE_ID_BITS = 5L;

    /**
     * 序列号位数
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 数据中心ID最大值 (2^5 - 1 = 31)
     */
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    /**
     * 机器ID最大值 (2^5 - 1 = 31)
     */
    private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);

    /**
     * 序列号最大值 (2^12 - 1 = 4095)
     */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    /**
     * 机器ID左移位数
     */
    private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 数据中心ID左移位数
     */
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;

    /**
     * 时间戳左移位数
     */
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS + DATACENTER_ID_BITS;

    /**
     * 数据中心ID
     */
    private final long datacenterId;

    /**
     * 机器ID
     */
    private final long machineId;

    /**
     * 序列号
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间戳
     */
    private long lastTimestamp = -1L;

    /**
     * 构造函数
     */
    public SnowflakeIdGenerator(
            @Value("${snowflake.datacenter-id:1}") long datacenterId,
            @Value("${snowflake.machine-id:1}") long machineId) {

        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("数据中心ID必须在0-%d之间", MAX_DATACENTER_ID));
        }

        if (machineId > MAX_MACHINE_ID || machineId < 0) {
            throw new IllegalArgumentException(
                    String.format("机器ID必须在0-%d之间", MAX_MACHINE_ID));
        }

        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * 生成下一个ID
     *
     * @return 唯一ID
     */
    public synchronized long nextId() {
        long timestamp = getCurrentTimestamp();

        // 系统时钟回退，自旋等待
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if(offset <= 5) {
                try {
                    Thread.sleep(offset >> 1);
                    timestamp = getCurrentTimestamp();
                    if (timestamp < lastTimestamp) {
                        throw new RuntimeException("系统时钟回退超出容忍限度，拒绝生成ID");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("等待始终恢复被中断，无法生成ID");
                }
            } else {
                throw new RuntimeException("系统时钟回退，拒绝生成ID");
            }
        }

        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 毫秒内序列溢出，等待下一毫秒
            if (sequence == 0) {
                timestamp = getNextTimestamp(lastTimestamp);
            }
        } else {
            // 时间戳改变，毫秒内序列重置
            sequence = 0L;
        }

        // 上次生成ID的时间戳
        lastTimestamp = timestamp;

        // 移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (machineId << MACHINE_ID_SHIFT)
                | sequence;
    }

    /**
     * 生成字符串格式的ID
     *
     * @return 字符串ID
     */
    public String nextIdStr() {
        return String.valueOf(nextId());
    }

    /**
     * 解析ID获取生成时间
     *
     * @param id 雪花ID
     * @return 生成时间戳
     */
    public long parseTimestamp(long id) {
        return (id >> TIMESTAMP_SHIFT) + START_TIMESTAMP;
    }

    /**
     * 解析ID获取数据中心ID
     *
     * @param id 雪花ID
     * @return 数据中心ID
     */
    public long parseDatacenterId(long id) {
        return (id >> DATACENTER_ID_SHIFT) & MAX_DATACENTER_ID;
    }

    /**
     * 解析ID获取机器ID
     *
     * @param id 雪花ID
     * @return 机器ID
     */
    public long parseMachineId(long id) {
        return (id >> MACHINE_ID_SHIFT) & MAX_MACHINE_ID;
    }

    /**
     * 解析ID获取序列号
     *
     * @param id 雪花ID
     * @return 序列号
     */
    public long parseSequence(long id) {
        return id & MAX_SEQUENCE;
    }

    /**
     * 获取当前时间戳
     *
     * @return 当前时间戳
     */
    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 获取下一毫秒时间戳
     *
     * @param lastTimestamp 上次时间戳
     * @return 下一毫秒时间戳
     */
    private long getNextTimestamp(long lastTimestamp) {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }

    /**
     * 获取生成器信息
     *
     * @return 生成器信息
     */
    public String getGeneratorInfo() {
        return String.format("SnowflakeIdGenerator[datacenterId=%d, machineId=%d]",
                datacenterId, machineId);
    }
}
