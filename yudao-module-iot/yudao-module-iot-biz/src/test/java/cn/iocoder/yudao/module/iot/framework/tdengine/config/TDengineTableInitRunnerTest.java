/**
 * 文件作用：验证 TDengine 初始化任务的本地启用条件。
 * 作者：DAMU
 * 创建时间：2026-07-23
 * 核心功能：防止未配置 TDengine 数据源时初始化任务连接到默认 MySQL 数据源。
 */
package cn.iocoder.yudao.module.iot.framework.tdengine.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.AnnotationUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link TDengineTableInitRunner} 的启用条件单元测试。
 *
 * @author DAMU
 */
public class TDengineTableInitRunnerTest {

    /**
     * 未显式启用 TDengine 时，初始化任务不应注册。
     */
    @Test
    public void testTdengineInitializationRequiresExplicitEnablement() {
        ConditionalOnProperty condition = AnnotationUtils.findAnnotation(
                TDengineTableInitRunner.class, ConditionalOnProperty.class);

        assertNotNull(condition);
        assertEquals("yudao.iot.tdengine", condition.prefix());
        assertEquals("enabled", condition.name()[0]);
        assertEquals("true", condition.havingValue());
        assertFalse(condition.matchIfMissing());
    }

}
