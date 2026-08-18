create table paymentservice.payment
(
    id uuid primary key default gen_random_uuid(),
    order_id uuid not null,
    user_id uuid not null,
    status         varchar(30),
    timestamp timestamptz default now(),
    payment_amount numeric(10, 2)
);

create index idx_user_id_order_id on paymentservice.payment (user_id, order_id);
create index idx_status on paymentservice.payment (status);
