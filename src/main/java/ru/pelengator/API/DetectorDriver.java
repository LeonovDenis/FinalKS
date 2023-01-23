package ru.pelengator.API;
import java.util.List;
/**
 * Абстракция драйверов детектора.
 * Драйвер детектора (или драйвер захвата, как он есть) — это фабрика для конкретных реализаций детекторов.

 */
public interface DetectorDriver{
    /**
     * Вернуть все зарегистрированные устройства.
     *
     * @return Список устройств
     */
    List<DetectorDevice> getDevices();

    /**
     * Является драйвером потокобезопасным. Операции с потокобезопасными драйверами не обязательно должны быть
     * синхронизированы.
     *
     * @return True, если драйвер является потокобезопасным, иначе false
     */
    boolean isThreadSafe();

}
