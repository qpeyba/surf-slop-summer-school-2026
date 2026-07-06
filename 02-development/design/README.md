# Реестр экранов — дизайн (Figma → «Шеф-стол»)

> Источник дизайн-макетов: [Figma — Волна приложение](https://www.figma.com/design/ySEt0cjmRqmhdWyDlTpDM5/%D0%92%D0%BE%D0%BB%D0%BD%D0%B0-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5?node-id=71-4857)
>
> Общий узел всех экранов: `?node-id=71-4857` (canvas «Экраны»)
>
> **Принцип адаптации:** Figma-макеты взяты из смежного домена (сёрф-школа «Волна»). Заимствуется общий дизайн: сетка, отступы, типографика. Цветовая палитра заменяется на палитру «Шеф-стол» (тёплые кулинарные тона). Контент и логика экранов — из `01-analysis/3-design-brief/`.

---

## Базовые дизайн-токены (из Figma)

| Токен | Значение |
|-------|----------|
| Canvas | 393×852 px (iPhone 14 Pro) |
| Шрифт основной | **Onest** (400 Regular, 700 Bold) |
| Шрифт системный | SF Pro (статус-бар), SF Compact (клавиатура) |
| Горизонтальный отступ | 16 px |
| Скругление карточек | 32 px |
| Скругление фото/полей | 16 px |
| Скругление кнопок | 32 px (pill) |
| Скругление чипсов/тегов | 8 px |
| Высота кнопок CTA | 56 px |
| Padding кнопок | 24 px вертикальный, 16 px горизонтальный |
| Padding полей ввода | 16 px |
| Padding карточки | 16 px |
| Межсекционный gap | 12 px (внутри карточки), 24 px (между секциями) |
| Отступ контента от статус-бара | y=80 px |
| Tab Bar | 12 px 32 px padding, blur(10.48px), 32 px border-radius |

### Типографическая шкала (Onest)

| Размер | Вес | Применение |
|--------|-----|------------|
| 32 px | Bold 700 | Заголовок экрана (h1) |
| 24 px | Bold 700 | Дата в карточке, название экрана в Top Bar |
| 20 px | Bold 700 | Подзаголовок в карточке |
| 18 px | Regular 400 | Подзаголовок-пояснение |
| 16 px | Regular 400 | Основной текст, поля ввода |
| 16 px | Bold 700 | Текст кнопок CTA |
| 14 px | Regular 400 | Второстепенный текст, подписи |
| 12 px | Regular 400 | Текст чипсов/тегов, мелкие подписи |

### Адаптация цветов

| Контекст Figma (Волна) | Замена для «Шеф-стол» |
|------------------------|------------------------|
| Brand/Primary `#00A59D` (teal/бирюзовый) | Тёплый терракотовый / оранжевый (кулинарная палитра) |
| Black `#161616` | Текст основной `#1A1A1A` |
| Grey `#797979` | Текст второстепенный `#757575` |
| Light grey `#F2F2F2` | `#F2F2F2` (оставить) |
| Green label `#92FF9A` | Зелёный для «Новичковый» |
| Yellow label `#FFF897` | Тёплый жёлтый / кремовый |
| Orange label `#FFD191` | Оранжевый для «Опытный» |
| Red `#FF6045` | Красный для ошибок/отмены |
| White `#FFFFFF` | `#FFFFFF` (оставить) |
| Secondary grey `#CCCCCC` | `#CCCCCC` (оставить) |

---

## Список экранов

### Основные экраны (SCR)

| ID | Название | Figma-аналог | Ссылка |
|----|----------|-------------|--------|
| SCR-001 | Расписание | Лоадер (расписание с данными) | [→ node-id=71-5687](https://www.figma.com/design/ySEt0cjmRqmhdWyDlTpDM5/%D0%92%D0%BE%D0%BB%D0%BD%D0%B0-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5?node-id=71-5687) |
| SCR-001 | Расписание (loading) | Загрузка списка | [→ node-id=71-5695](https://www.figma.com/design/ySEt0cjmRqmhdWyDlTpDM5/%D0%92%D0%BE%D0%BB%D0%BD%D0%B0-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5?node-id=71-5695) |
| SCR-001 | Расписание (empty) | Нет прогулок | [→ node-id=71-6002](https://www.figma.com/design/ySEt0cjmRqmhdWyDlTpDM5/%D0%92%D0%BE%D0%BB%D0%BD%D0%B0-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5?node-id=71-6002) |
| SCR-001 | Расписание (error) | Ошибка загрузки | [→ node-id=71-6026](https://www.figma.com/design/ySEt0cjmRqmhdWyDlTpDM5/%D0%92%D0%BE%D0%BB%D0%BD%D0%B0-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5?node-id=71-6026) |
| SCR-002 | Карточка класса | Карточка прогулки | [→ node-id=71-6169](https://www.figma.com/design/ySEt0cjmRqmhdWyDlTpDM5/%D0%92%D0%BE%D0%BB%D0%BD%D0%B0-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5?node-id=71-6169) |
| SCR-002 | Карта маршрута | Карта маршрута | [→ node-id=71-6176](https://www.figma.com/design/ySEt0cjmRqmhdWyDlTpDM5/%D0%92%D0%BE%D0%BB%D0%BD%D0%B0-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5?node-id=71-6176) |
| SCR-003 | Форма бронирования | Оформление записи | [→ node-id=71-6145](https://www.figma.com/design/ySEt0cjmRqmhdWyDlTpDM5/%D0%92%D0%BE%D0%BB%D0%BD%D0%B0-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5?node-id=71-6145) |
| SCR-004 | Успешная запись | Запись подтверждена | [→ node-id=71-6161](https://www.figma.com/design/ySEt0cjmRqmhdWyDlTpDM5/%D0%92%D0%BE%D0%BB%D0%BD%D0%B0-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5?node-id=71-6161) |
| SCR-005 | Мои брони | Список «Мои записи» | [→ node-id=71-6085](https://www.figma.com/design/ySEt0cjmRqmhdWyDlTpDM5/%D0%92%D0%BE%D0%BB%D0%BD%D0%B0-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5?node-id=71-6085) |
| SCR-005 | Детали брони | Детали записи | [→ node-id=71-6100](https://www.figma.com/design/ySEt0cjmRqmhdWyDlTpDM5/%D0%92%D0%BE%D0%BB%D0%BD%D0%B0-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5?node-id=71-6100) |
| SCR-006 | Авторизация (ввод номера) | Ввод номера | [→ node-id=71-5377](https://www.figma.com/design/ySEt0cjmRqmhdWyDlTpDM5/%D0%92%D0%BE%D0%BB%D0%BD%D0%B0-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5?node-id=71-5377) |
| SCR-006 | Авторизация (пустой ввод) | Вход по номеру телефона | [→ node-id=71-5401](https://www.figma.com/design/ySEt0cjmRqmhdWyDlTpDM5/%D0%92%D0%BE%D0%BB%D0%BD%D0%B0-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5?node-id=71-5401) |
| SCR-006 | Авторизация (OTP-код) | Ввод кода | [→ node-id=71-12353](https://www.figma.com/design/ySEt0cjmRqmhdWyDlTpDM5/%D0%92%D0%BE%D0%BB%D0%BD%D0%B0-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5?node-id=71-12353) |
| SCR-006 | Авторизация (повтор кода) | Отправить код повторно | [→ node-id=71-5504](https://www.figma.com/design/ySEt0cjmRqmhdWyDlTpDM5/%D0%92%D0%BE%D0%BB%D0%BD%D0%B0-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5?node-id=71-5504) |
| SCR-006 | Регистрация (имя) | Регистрация | [→ node-id=71-5549](https://www.figma.com/design/ySEt0cjmRqmhdWyDlTpDM5/%D0%92%D0%BE%D0%BB%D0%BD%D0%B0-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5?node-id=71-5549) |
| SCR-007 | Профиль | Профиль | [→ node-id=71-6677](https://www.figma.com/design/ySEt0cjmRqmhdWyDlTpDM5/%D0%92%D0%BE%D0%BB%D0%BD%D0%B0-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5?node-id=71-6677) |
| SCR-007 | Профиль (редактирование) | Изменение профиля | [→ node-id=71-6690](https://www.figma.com/design/ySEt0cjmRqmhdWyDlTpDM5/%D0%92%D0%BE%D0%BB%D0%BD%D0%B0-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5?node-id=71-6690) |

### Шторки / модальные окна (BS)

| ID | Название | Figma-аналог | Ссылка |
|----|----------|-------------|--------|
| BS-001 | Отмена брони | Отмена записи | [→ node-id=71-13106](https://www.figma.com/design/ySEt0cjmRqmhdWyDlTpDM5/%D0%92%D0%BE%D0%BB%D0%BD%D0%B0-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5?node-id=71-13106) |
| BS-002 | Перенос брони | — (нет прямого аналога) | Комбинируется из SCR-001 + BS-001 |
| BS-003 | Оценка шефа | — (нет прямого аналога) | Комбинируется из BS-001 (шторка) |
| BS-004 | Фильтр дат | Лоадер (фильтры по умолчанию) | [→ node-id=71-5712](https://www.figma.com/design/ySEt0cjmRqmhdWyDlTpDM5/%D0%92%D0%BE%D0%BB%D0%BD%D0%B0-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5?node-id=71-5712) |
| BS-004 | Фильтр дат (активные) | Лоадер (фильтры активны) | [→ node-id=71-5722](https://www.figma.com/design/ySEt0cjmRqmhdWyDlTpDM5/%D0%92%D0%BE%D0%BB%D0%BD%D0%B0-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5?node-id=71-5722) |

---

## Структура директории

```
design/
├── README.md                  ← этот файл
├── SCR-001_Schedule.md        ← Расписание
├── SCR-002_ClassDetail.md     ← Карточка класса
├── SCR-003_BookingForm.md     ← Форма бронирования
├── SCR-004_BookingSuccess.md  ← Успешная запись
├── SCR-005_MyBookings.md      ← Мои брони
├── SCR-006_Auth.md            ← Авторизация
├── SCR-007_Profile.md         ← Профиль
├── BS-001_CancelConfirm.md    ← Отмена брони
├── BS-002_TransferSelect.md   ← Перенос брони
├── BS-003_RateChef.md         ← Оценка шефа
└── BS-004_FilterDate.md       ← Фильтр дат
```
