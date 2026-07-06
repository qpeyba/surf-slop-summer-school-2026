# FE-04. Shared UI-компоненты

> Оценка: 1.5 дня

## Требования ко всем компонентам

- Material 3 тема
- Content description (accessibility)
- Preview (Compose Preview)
- Touch targets ≥ 48x48dp

## Задачи (по группам)

- [ ] **Buttons:** `ChefButton` (Primary — terracotta, Secondary — outlined, Destructive — red)
- [ ] **Inputs:** `PhoneInput` (маска `+7 (XXX) XXX-XX-XX`), `OtpInput` (4 бокса, авто-переход, авто-submit), `StarRating` (интерактивная), `StarRatingDisplay` (read-only)
- [ ] **Cards:** `ClassCard` (фото, меню, шеф, сложность, цена, места), `BookingCard` (программа, дата, шеф, статус, кнопки действий), `ChefInfoRow`, `AddressRow`
- [ ] **States:** `EmptyState` (иллюстрация + текст + кнопка), `ErrorState` (ошибка + Retry), `LoadingSkeleton` (3 варианта: list — карточки, card — одна карточка, detail — экран деталей)
- [ ] **Badges:** `DifficultyBadge` («Для новичков» зелёный, «Для опытных» оранжевый), `StatusBadge` (Активна — синий, Отменена — серый, Завершена — зелёный, КлиентНеПришёл — красный)
- [ ] **Progress:** `SpotsProgressBar` («3 из 12 мест», заполнение относительно capacity), `LoyaltyProgressBar` (уровни «Новичок» / «Серебро» / «Золото»)
- [ ] **Dates:** `DateStrip` (горизонтальная полоса на 7 дат, `LazyRow`, стрелки влево/вправо, сегодня выделено акцентом)
- [ ] **Media:** `PhotoCarousel` (`HorizontalPager`, Coil, индикатор текущей страницы)
- [ ] **Misc:** `ChefTopAppBar` (заголовок + иконки), `ChefBottomBar` (3 таба), `ChefChip` (аллергия с крестиком), `EquipmentSelector` (radio-группа), `PriceTag` (форматирование «3 500 ₽»), `CountdownTimer` (секунды до resend)

## Маппинг API

*Не используется*

## Критерий приёмки

- Все компоненты отображаются в `@Preview`
- Скриншоты соответствуют дизайн-токенам:
  - Базовый фрейм: 412×915 dp
  - Отступы экрана: 16dp горизонтальные
  - Радиус карточек: 12dp
  - Высота кнопок: 48dp
  - Шрифт: Roboto (системный)
  - Цвета из `Color.kt` (terracotta, olive, cream, surface `#FAFAFA`, card `#FFFFFF`)
- Все иконки имеют contentDescription
- `LoadingSkeleton` имеет shimmer-анимацию
