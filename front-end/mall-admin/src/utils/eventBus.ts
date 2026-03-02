import mitt from 'mitt'

type Events = {
  'menu:toggleCollapse': void
}

const eventBus = mitt<Events>()

export default eventBus
