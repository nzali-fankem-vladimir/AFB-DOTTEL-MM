export function PageHeader({ surTitre, titre }) {
  return (
    <section className="border-b border-neutral-200 bg-white px-8 py-6">
      <div className="flex items-end justify-between gap-4">
        <div>
          <p className="mb-1 text-xxs font-semibold uppercase tracking-[0.2em] text-neutral-600">
            {surTitre}
          </p>
          {/* text-pretty : evite qu'un titre long ("Administration des
              utilisateurs") ne laisse un mot seul sur la derniere ligne quand
              la fenetre se reduit. */}
          <h1 className="text-2xl font-bold text-pretty text-neutral-900">{titre}</h1>
        </div>
        <div className="hidden h-10 w-1 rounded-full bg-linear-to-b from-primary-500 to-transparent sm:block" />
      </div>
    </section>
  );
}
